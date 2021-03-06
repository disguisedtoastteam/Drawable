import { isPlatform, setupPlatforms } from '../utils/platform';
import { Config, configFromSession, configFromURL, saveConfig } from './config';
const win = typeof window !== 'undefined' ? window : {};
const Ionic = win['Ionic'] = win['Ionic'] || {};
Object.defineProperty(Ionic, 'queue', {
    get: () => Context['queue']
});
setupPlatforms(win);
Context.isPlatform = isPlatform;
const configObj = Object.assign({}, configFromSession(win), { persistConfig: false }, Ionic['config'], configFromURL(win));
const config = Ionic['config'] = Context['config'] = new Config(configObj);
if (config.getBoolean('persistConfig')) {
    saveConfig(win, configObj);
}
const documentElement = win.document ? win.document.documentElement : null;
const mode = config.get('mode', (documentElement && documentElement.getAttribute('mode')) || (isPlatform(win, 'ios') ? 'ios' : 'md'));
Ionic.mode = Context.mode = mode;
config.set('mode', mode);
if (documentElement) {
    documentElement.setAttribute('mode', mode);
    documentElement.classList.add(mode);
}
if (config.getBoolean('_testing')) {
    config.set('animated', false);
}
