param(
 [string]$DependencyWar='C:\gitsrc\traveller-v1-release\target\TravellerCharacterGeneration.war',
 [string]$DependencyLibraries='C:\gitsrc\traveller-v1-release\target\TravellerCharacterGeneration\WEB-INF\lib',
 [string]$JavaHome='C:\Program Files\Java\jdk-25',
 [string]$OutputWar=''
)
$ErrorActionPreference='Stop'
$shipRoot=$PSScriptRoot
New-Item -ItemType Directory -Force (Join-Path $shipRoot 'target\classes') | Out-Null
$sources=Get-ChildItem (Join-Path $shipRoot 'src\main\java') -Recurse -Filter *.java | ForEach-Object {$_.FullName}
& "$JavaHome\bin\javac.exe" --release 11 -parameters -encoding UTF-8 -cp "$DependencyLibraries\*" -d (Join-Path $shipRoot 'target\classes') $sources
if($LASTEXITCODE -ne 0){throw 'Compilation failed'}
Copy-Item (Join-Path $shipRoot 'src\main\resources\*') (Join-Path $shipRoot 'target\classes') -Recurse -Force
$archivePath=if($OutputWar){$OutputWar}else{Join-Path $shipRoot 'target\TravellerWorldBuilder.war'}
Copy-Item -LiteralPath $DependencyWar -Destination $archivePath -Force
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive=[IO.Compression.ZipFile]::Open($archivePath,[IO.Compression.ZipArchiveMode]::Update)
try {
 $oldEntries=@($archive.Entries | Where-Object {$_.FullName.StartsWith('WEB-INF/classes/')})
 foreach($entry in $oldEntries){$entry.Delete()}
 $archive.CreateEntry('WEB-INF/classes/') | Out-Null
 $manifestEntry=$archive.GetEntry('META-INF/MANIFEST.MF')
 $reader=[IO.StreamReader]::new($manifestEntry.Open());$manifestText=$reader.ReadToEnd();$reader.Dispose();$manifestEntry.Delete()
 $manifestText=[regex]::Replace($manifestText,'Start-Class:[^\r\n]*(?:\r?\n [^\r\n]*)*','Start-Class: com.javasmyths.worldbuilder.WorldBuilderApplication')
 $manifestText=[regex]::Replace($manifestText,'Implementation-Title:[^\r\n]*','Implementation-Title: TravellerWorldBuilder')
 $manifestText=[regex]::Replace($manifestText,'Implementation-Version:[^\r\n]*','Implementation-Version: 0.1.0')
 $manifestEntry=$archive.CreateEntry('META-INF/MANIFEST.MF');$writer=[IO.StreamWriter]::new($manifestEntry.Open(),[Text.UTF8Encoding]::new($false));$writer.Write($manifestText);$writer.Dispose()
 $classRoot=Join-Path $shipRoot 'target\classes'
 Get-ChildItem -LiteralPath $classRoot -Recurse -Directory | ForEach-Object {
  $relative=$_.FullName.Substring($classRoot.Length+1).Replace('\','/')
  $archive.CreateEntry('WEB-INF/classes/'+$relative+'/') | Out-Null
 }
 Get-ChildItem -LiteralPath $classRoot -Recurse -File | ForEach-Object {
  $relative=$_.FullName.Substring($classRoot.Length+1).Replace('\','/')
  [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive,$_.FullName,'WEB-INF/classes/'+$relative,[IO.Compression.CompressionLevel]::Optimal) | Out-Null
 }
}finally{$archive.Dispose()}
Get-FileHash -LiteralPath $archivePath
