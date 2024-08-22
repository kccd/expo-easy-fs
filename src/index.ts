import { NativeModulesProxy, EventEmitter, Subscription } from 'expo-modules-core';

// Import the native module. On web, it will be resolved to ExpoEasyFs.web.ts
// and on native platforms to ExpoEasyFs.ts
import ExpoEasyFsModule from './ExpoEasyFsModule';
import ExpoEasyFsView from './ExpoEasyFsView';
import { ChangeEventPayload, ExpoEasyFsViewProps } from './ExpoEasyFs.types';

// Get the native constant value.
export const PI = ExpoEasyFsModule.PI;

export function hello(): string {
  return ExpoEasyFsModule.hello();
}

export async function setValueAsync(value: string) {
  return await ExpoEasyFsModule.setValueAsync(value);
}

const emitter = new EventEmitter(ExpoEasyFsModule ?? NativeModulesProxy.ExpoEasyFs);

export function addChangeListener(listener: (event: ChangeEventPayload) => void): Subscription {
  return emitter.addListener<ChangeEventPayload>('onChange', listener);
}

export { ExpoEasyFsView, ExpoEasyFsViewProps, ChangeEventPayload };
