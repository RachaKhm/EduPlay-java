Add-Type -AssemblyName System.Speech
$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer
Register-ObjectEvent -InputObject $synth -EventName SpeakProgress -Action {
    Write-Host "WORD:$($EventArgs.CharacterPosition):$($EventArgs.CharacterCount)"
}
$synth.Speak("Bonjour tout le monde")
Start-Sleep -Seconds 1
