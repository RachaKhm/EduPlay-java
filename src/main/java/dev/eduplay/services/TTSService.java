package dev.eduplay.services;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class TTSService {
    private Process currentProcess;
    private Path currentCmdDir;
    private int currentVolume = 100;

    /**
     * Récupère la liste des voix installées sur le système Windows.
     */
    public static List<String> getAvailableVoices() {
        List<String> voices = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("powershell", "-Command", 
                "Add-Type -AssemblyName System.Speech; $synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; foreach($v in $synth.GetInstalledVoices()) { Write-Host \"$($v.VoiceInfo.Name)|$($v.VoiceInfo.Culture)\" }");
            Process p = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        voices.add(line.trim());
                    }
                }
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return voices;
    }

    /**
     * Lit le texte à haute voix en utilisant le TTS de Windows.
     *
     * @param text L'intégralité du texte à lire.
     * @param voiceName Nom de la voix à utiliser (peut être null).
     * @param speed Vitesse de lecture (de -10 à 10). 0 est la normale.
     * @param onWordRead Callback (start, length) appelé quand un mot commence à être lu.
     * @param onFinished Callback appelé quand la lecture est terminée.
     */
    public void readText(String text, String voiceName, int speed, BiConsumer<Integer, Integer> onWordRead, Runnable onFinished) {
        stop(); // Arrêter la lecture précédente s'il y en a une

        Thread t = new Thread(() -> {
            try {
                Path textPath = Files.createTempFile("tts_text_", ".txt");
                File textFile = textPath.toFile();
                textFile.deleteOnExit();
                Files.writeString(textPath, text, StandardCharsets.UTF_8);

                Path eventPath = Files.createTempFile("tts_events_", ".txt");
                File eventFile = eventPath.toFile();
                eventFile.deleteOnExit();

                Path scriptPath = Files.createTempFile("tts_script_", ".ps1");
                File scriptFile = scriptPath.toFile();
                scriptFile.deleteOnExit();

                currentCmdDir = Files.createTempDirectory("tts_cmds_");
                currentCmdDir.toFile().deleteOnExit();

                try (FileWriter writer = new FileWriter(scriptFile, StandardCharsets.UTF_8)) {
                    writer.write("$OutputEncoding = [console]::InputEncoding = [console]::OutputEncoding = New-Object System.Text.UTF8Encoding\n");
                    writer.write("Add-Type -AssemblyName System.Speech\n");
                    writer.write("$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer\n");
                    
                    if (voiceName != null && !voiceName.isEmpty()) {
                        // Échapper les guillemets simples dans le nom de la voix
                        writer.write("try { $synth.SelectVoice('" + voiceName.replace("'", "''") + "') } catch {}\n");
                    }
                    writer.write("$synth.Rate = " + speed + "\n");
                    writer.write("$synth.Volume = " + currentVolume + "\n");

                    writer.write("$cmdDir = '" + currentCmdDir.toAbsolutePath().toString().replace("'", "''") + "'\n");
                    
                    writer.write("Register-ObjectEvent -InputObject $synth -EventName SpeakProgress -Action {\n");
                    // On utilise Out-File en ASCII pour éviter les problèmes de BOM avec RandomAccessFile
                    writer.write("    \"WORD:$($EventArgs.CharacterPosition):$($EventArgs.CharacterCount)\" | Out-File -FilePath '" + eventFile.getAbsolutePath().replace("'", "''") + "' -Append -Encoding ASCII\n");
                    writer.write("}\n");

                    writer.write("Register-ObjectEvent -InputObject $synth -EventName SpeakCompleted -Action {\n");
                    writer.write("    \"COMPLETED\" | Out-File -FilePath '" + eventFile.getAbsolutePath().replace("'", "''") + "' -Append -Encoding ASCII\n");
                    writer.write("}\n");

                    writer.write("$text = Get-Content -Path '" + textFile.getAbsolutePath().replace("'", "''") + "' -Raw -Encoding UTF8\n");
                    writer.write("$prompt = $synth.SpeakAsync($text)\n");
                    
                    writer.write("while ($prompt.IsCompleted -eq $false) {\n");
                    writer.write("    if (Test-Path \"$cmdDir\\pause\") { $synth.Pause(); Remove-Item \"$cmdDir\\pause\" -ErrorAction SilentlyContinue }\n");
                    writer.write("    if (Test-Path \"$cmdDir\\resume\") { $synth.Resume(); Remove-Item \"$cmdDir\\resume\" -ErrorAction SilentlyContinue }\n");
                    writer.write("    if (Test-Path \"$cmdDir\\stop\") { $synth.SpeakAsyncCancelAll(); break }\n");
                    writer.write("    $volFiles = Get-ChildItem \"$cmdDir\\volume_*\" -ErrorAction SilentlyContinue\n");
                    writer.write("    if ($volFiles) {\n");
                    writer.write("        $lastVol = $volFiles[-1].Name.Substring(7)\n");
                    writer.write("        if ($lastVol -match '^\\d+$') { $synth.Volume = [int]$lastVol }\n");
                    writer.write("        $volFiles | Remove-Item -ErrorAction SilentlyContinue\n");
                    writer.write("    }\n");
                    writer.write("    Start-Sleep -Milliseconds 50\n");
                    writer.write("}\n");
                    writer.write("Start-Sleep -Milliseconds 200\n"); // Laisse le temps au dernier event de s'afficher
                }

                ProcessBuilder pb = new ProcessBuilder("powershell", "-ExecutionPolicy", "Bypass", "-File", scriptFile.getAbsolutePath());
                currentProcess = pb.start();

                boolean completed = false;
                // Lire le fichier d'événements en temps réel
                try (java.io.RandomAccessFile reader = new java.io.RandomAccessFile(eventFile, "r")) {
                    while (currentProcess.isAlive() || reader.getFilePointer() < reader.length()) {
                        String line = reader.readLine();
                        if (line != null) {
                            if (line.startsWith("WORD:")) {
                                String[] parts = line.split(":");
                                if (parts.length == 3) {
                                    try {
                                        int start = Integer.parseInt(parts[1]);
                                        int length = Integer.parseInt(parts[2]);
                                        if (onWordRead != null) {
                                            Platform.runLater(() -> onWordRead.accept(start, length));
                                        }
                                    } catch (NumberFormatException ignored) {}
                                }
                            } else if (line.equals("COMPLETED")) {
                                completed = true;
                                break;
                            }
                        } else {
                            Thread.sleep(20); // Attente courte
                        }
                    }
                }

                if (!completed) {
                    currentProcess.waitFor();
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (onFinished != null) {
                    Platform.runLater(onFinished);
                }
            }
        });

        t.setDaemon(true);
        t.start();
    }

    public void pause() {
        if (currentCmdDir != null) {
            try { Files.createFile(currentCmdDir.resolve("pause")); } catch(Exception e) {}
        }
    }

    public void resume() {
        if (currentCmdDir != null) {
            try { Files.createFile(currentCmdDir.resolve("resume")); } catch(Exception e) {}
        }
    }

    public void setVolume(int volume) {
        this.currentVolume = volume;
        if (currentCmdDir != null) {
            try {
                // Nettoyer les anciens fichiers volume_*
                try (var stream = Files.newDirectoryStream(currentCmdDir, "volume_*")) {
                    for (Path p : stream) {
                        Files.deleteIfExists(p);
                    }
                }
                Files.createFile(currentCmdDir.resolve("volume_" + volume));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        if (currentCmdDir != null) {
            try { Files.createFile(currentCmdDir.resolve("stop")); } catch(Exception e) {}
        }
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroyForcibly();
        }
    }
}
