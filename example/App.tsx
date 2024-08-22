import { StyleSheet, Text, View } from 'react-native';

import * as ExpoEasyFs from 'expo-easy-fs';

export default function App() {
  return (
    <View style={styles.container}>
      <Text>{ExpoEasyFs.hello()}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
