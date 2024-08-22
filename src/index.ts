import { Platform } from "react-native";

import ExpoEasyFsModule from "./ExpoEasyFsModule";

export function copyFileToDownload(
  uri: string,
  filename: string,
): Promise<void> {
  if (Platform.OS !== "android") {
    throw new Error(`Not yet supported on ${Platform.OS}`);
  }
  return ExpoEasyFsModule.copyFileToDownload(uri, filename);
}
