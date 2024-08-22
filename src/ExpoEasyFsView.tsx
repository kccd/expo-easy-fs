import { requireNativeViewManager } from 'expo-modules-core';
import * as React from 'react';

import { ExpoEasyFsViewProps } from './ExpoEasyFs.types';

const NativeView: React.ComponentType<ExpoEasyFsViewProps> =
  requireNativeViewManager('ExpoEasyFs');

export default function ExpoEasyFsView(props: ExpoEasyFsViewProps) {
  return <NativeView {...props} />;
}
