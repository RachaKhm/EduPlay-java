Add-Type -AssemblyName System.Speech
$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer
$synth.Volume = 100
$prompt = $synth.SpeakAsync('Ceci est un test de volume très long pour voir si cela change en direct, car je veux savoir si le volume peut être modifié pendant la lecture. Je vais baisser le volume dans deux secondes.')
Start-Sleep -Seconds 2
$synth.Volume = 10
Write-Host "Volume baissé"
while ($prompt.IsCompleted -eq $false) { Start-Sleep -Milliseconds 100 }
