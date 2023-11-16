import type { TurboModule } from "react-native/Libraries/TurboModule/RCTExport";
import { TurboModuleRegistry } from "react-native";

export interface Spec extends TurboModule {
    startPsiphon(): Promise<boolean>;
    stopPsiphon(): void;
    fetch(method: string, url: string, body: string | null, useProxy: boolean): Promise<string>;
    addListener: (eventType: string) => void;
    removeListeners(count: number): void;
}
export default TurboModuleRegistry.getEnforcing<Spec>(
    'RTNPsiphonFetch',
  );