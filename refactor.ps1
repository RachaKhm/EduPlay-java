$ErrorActionPreference = "Stop"

$dir = "c:\Users\user\IdeaProjects\EduPlay-java\src\main"

# 1. Rename content in files
Get-ChildItem -Path $dir -Recurse -Include *.java,*.fxml | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    if ($null -ne $content) {
        $newContent = $content -creplace "Books", "Resources" `
                              -creplace "books", "resources" `
                              -creplace "Book", "Resource" `
                              -creplace "book", "resource"
        if ($content -cne $newContent) {
            Set-Content -Path $_.FullName -Value $newContent -NoNewline
            Write-Host "Updated content in $($_.FullName)"
        }
    }
}

# 2. Rename files
Get-ChildItem -Path $dir -Recurse -File | Where-Object { $_.Name -cmatch "Book" -or $_.Name -cmatch "book" } | ForEach-Object {
    $newName = $_.Name -creplace "Book", "Resource" -creplace "book", "resource"
    Rename-Item -Path $_.FullName -NewName $newName
    Write-Host "Renamed file $($_.Name) to $newName"
}

Write-Host "Refactoring complete."
