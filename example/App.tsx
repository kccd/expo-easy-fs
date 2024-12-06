import * as ExpoEasyFs from "expo-easy-fs";
import * as fileSystem from "expo-file-system";
import { StyleSheet, Text, View, StatusBar, Button } from "react-native";

export default function App() {
  return (
    <View style={styles.container}>
      <StatusBar barStyle="dark-content" />
      <Text>Hello world</Text>
      <Button
        title="Copy file to download directory"
        onPress={() => {
          const filename = `expo-easy-fs_${Date.now()}.txt`;
          const filePath = fileSystem.documentDirectory + filename;
          fileSystem
            .writeAsStringAsync(filePath, filename)
            .then(() => {
              return ExpoEasyFs.copyFileToDownload(filePath, filename);
            })
            .then(() => {
              alert("Copied!");
            })
            .catch((err) => {
              alert(err.message);
            });
        }}
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
