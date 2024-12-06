import * as ExpoEasyFs from "expo-easy-fs";
import * as fileSystem from "expo-file-system";
import { StyleSheet, Text, View, StatusBar, Button } from "react-native";
import { useCallback } from "react";

export default function App() {
  const func = useCallback(async () => {
    try {
      const filename = `expo-easy-fs-${Date.now()}.txt`;
      const originFilePath = fileSystem.documentDirectory + filename;

      const { downloads: systemDownloadsDir } = await ExpoEasyFs.getPaths();

      const destinationDirPath = systemDownloadsDir + `/expo-easy-fs/`;
      const destinationFilePath = destinationDirPath + filename;

      await fileSystem.writeAsStringAsync(originFilePath, "Test");
      await ExpoEasyFs.mkdir(destinationDirPath);
      await ExpoEasyFs.copyFile(originFilePath.replace(/^file:\//, ""), destinationFilePath);
      await ExpoEasyFs.remove(originFilePath);
      alert(`Success! The test file has been created and moved to: ${destinationFilePath}`);
    } catch (err) {
      console.error(err);
      alert("An error occurred while processing the file. Please try again.");
    }
  }, []);

  return (
    <View style={styles.container}>
      <StatusBar barStyle="dark-content" />
      <Text>Welcome to the Expo Easy FS Demo</Text>
      <Button
        title="Create a file and move it to the system's Downloads folder"
        onPress={func}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#fff",
    alignItems: "center",
    justifyContent: "center",
  },
});
