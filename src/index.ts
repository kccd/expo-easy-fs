import ExpoEasyFsModule from "./ExpoEasyFsModule";

export function copyFileToDownload(
  uri: string,
  filename: string,
): Promise<void> {
  return ExpoEasyFsModule.copyFileToDownload(uri, filename);
}
