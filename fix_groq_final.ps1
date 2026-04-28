$path = 'C:\Users\user\IdeaProjects\EduPlay-java\src\main\java\dev\eduplay\services\GroqService.java'
# Read bytes to check for BOM
$bytes = [System.IO.File]::ReadAllBytes($path)
if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
    Write-Host "BOM detected, removing..."
    $bytes = $bytes[3..($bytes.Length-1)]
}

# Convert back to string to replace the bad comment
$content = [System.Text.Encoding]::UTF8.GetString($bytes)
$content = $content.Replace('// \uXXXX', '// JSON unicode escape sequence')

# Write back without BOM
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($path, $content, $utf8NoBom)
Write-Host "Fixed GroqService.java (BOM removed and comment fixed)"
