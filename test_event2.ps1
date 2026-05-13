$OutputEncoding = [console]::InputEncoding = [console]::OutputEncoding = New-Object System.Text.UTF8Encoding
Add-Type -AssemblyName System.Speech
$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer
Register-ObjectEvent -InputObject $synth -EventName SpeakProgress -Action {
    Write-Output "WORD:$($EventArgs.CharacterPosition):$($EventArgs.CharacterCount)"
}
$synth.Speak("Bonjour tout le monde")
Start-Sleep -Seconds 1
