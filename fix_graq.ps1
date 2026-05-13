$path = 'C:\Users\user\IdeaProjects\EduPlay-java\src\main\java\dev\eduplay\services\GroqService.java'
$lines = [System.IO.File]::ReadAllLines($path, [System.Text.Encoding]::UTF8)
Write-Host "Total lines: $($lines.Length)"
Write-Host "Line 194 before: $($lines[193])"

# Replace line 194 (index 193) - remove the illegal \uXXXX comment
$lines[193] = "                        case 'u':  // JSON unicode escape sequence"

Write-Host "Line 194 after:  $($lines[193])"
[System.IO.File]::WriteAllLines($path, $lines, [System.Text.Encoding]::UTF8)
Write-Host "Done."
