import ExpoEasyFsModule from "./ExpoEasyFsModule";

// Deprecated
export function copyFileToDownload(
  uri: string,
  filename: string,
): Promise<void> {
  return ExpoEasyFsModule.copyFileToDownload(uri, filename);
}

export function fixPath(filePath: string) {
  if(filePath.indexOf('file:///') === 0) {
    return filePath.slice(6);
  }
  return filePath
}

export function getPaths(): Promise<{
  downloads: string
}> {
  return ExpoEasyFsModule.getPaths();
}

export function mkdir(dirPath: string): Promise<String> {
  return ExpoEasyFsModule.mkdir(fixPath(dirPath));
}

export function copyFile(sourcePath: string, destinationPath: string): Promise<string> {
  return ExpoEasyFsModule.copyFile(fixPath(sourcePath), fixPath(destinationPath));
}

export function remove(targetPath: string): Promise<string> {
  return ExpoEasyFsModule.remove(fixPath(targetPath));
}

export function exists(targetPath: string): Promise<{
  exists: boolean;
  type: 'file' | 'directory' | 'unknown';
  path: string;
}> {
  return ExpoEasyFsModule.exists(fixPath(targetPath));
}