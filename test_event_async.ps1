$OutputEncoding = [console]::InputEncoding = [console]::OutputEncoding = New-Object System.Text.UTF8Encoding
Add-Type -AssemblyName System.Speech
$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer
Remove-Item -Path "events.txt" -ErrorAction Ignore
Register-ObjectEvent -InputObject $synth -EventName SpeakProgress -Action {
    "WORD:$($EventArgs.CharacterPosition):$($EventArgs.CharacterCount)" | Out-File -FilePath "events.txt" -Append -Encoding ASCII
}
$prompt = $synth.SpeakAsync("Bonjour tout le monde. Ceci est un test de lecture très long pour voir.")
while (-not $prompt.IsCompleted) {
    Start-Sleep -Milliseconds 50
}
