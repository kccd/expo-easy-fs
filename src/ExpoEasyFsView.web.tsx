import * as React from 'react';

import { ExpoEasyFsViewProps } from './ExpoEasyFs.types';

export default function ExpoEasyFsView(props: ExpoEasyFsViewProps) {
  return (
    <div>
      <span>{props.name}</span>
    </div>
  );
}
