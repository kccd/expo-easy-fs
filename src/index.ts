import ExpoEasyFsModule from "./ExpoEasyFsModule";

export function copyFileToDownload(
  uri: string,
  filename: string,
): Promise<void> {
  return ExpoEasyFsModule.copyFileToDownload(uri, filename);
}

export function getPaths(): Promise<{
  downloads: string
}> {
  return ExpoEasyFsModule.getPaths();
}

export function mkdir(dirPath: string): Promise<String> {
  return ExpoEasyFsModule.mkdir(dirPath);
}

export function copyFile(sourcePath: string, destinationPath: string): Promise<string> {
  return ExpoEasyFsModule.copyFile(sourcePath, destinationPath);
}

export function remove(targetPath: string): Promise<string> {
  return ExpoEasyFsModule.remove(targetPath);
}

export function exists(targetPath: string): Promise<{
  exists: boolean;
  type: 'file' | 'directory' | 'unknown';
  path: string;
}> {
  return ExpoEasyFsModule.exists(targetPath);
}