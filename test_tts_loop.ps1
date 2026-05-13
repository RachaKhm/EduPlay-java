$cmdDir = "C:\Users\user\IdeaProjects\EduPlay-java\tts_cmds_test"
if (-not (Test-Path $cmdDir)) { New-Item -ItemType Directory -Path $cmdDir | Out-Null }
Add-Type -AssemblyName System.Speech
$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer
$synth.Volume = 100
$prompt = $synth.SpeakAsync('Je vais tester la boucle de changement de volume asynchrone pour voir si le fichier texte est correctement lu et le volume mis à jour.')

while ($prompt.IsCompleted -eq $false) {
    if (Test-Path "$cmdDir\volume.txt") {
        try {
            $v = Get-Content "$cmdDir\volume.txt" -ErrorAction SilentlyContinue
            Write-Host "Read volume.txt: '$v'"
            if ($v -ne $null -and $v -ne '') { 
                $synth.Volume = [int]$v 
                Write-Host "Set volume to $v"
            }
            Remove-Item "$cmdDir\volume.txt" -ErrorAction SilentlyContinue
        } catch {
            Write-Host "Error: $_"
        }
    }
    Start-Sleep -Milliseconds 50
}
Write-Host "Done"
