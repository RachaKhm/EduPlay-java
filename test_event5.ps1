$OutputEncoding = [console]::InputEncoding = [console]::OutputEncoding = New-Object System.Text.UTF8Encoding
Add-Type -AssemblyName System.Speech
$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer
Remove-Item -Path "events.txt" -ErrorAction Ignore
Register-ObjectEvent -InputObject $synth -EventName SpeakProgress -Action {
    "WORD:$($EventArgs.CharacterPosition):$($EventArgs.CharacterCount)" | Out-File -FilePath "events.txt" -Append -Encoding UTF8
}
$synth.Speak("Bonjour tout le monde")
Start-Sleep -Seconds 1
