export{b as getPlatforms,a as isPlatform,c as setupPlatforms,d as testUserAgent,e as PLATFORMS_MAP}from "./chunk-f0b1119d.js";export{b as LIFECYCLE_WILL_ENTER,c as LIFECYCLE_DID_ENTER,a as LIFECYCLE_WILL_LEAVE,d as LIFECYCLE_DID_LEAVE,e as LIFECYCLE_WILL_UNLOAD}from "./chunk-90d954cd.js";function setupConfig(o){var n=window,i=n.Ionic;if(!i||!i.config||"Object"===i.config.constructor.name)return n.Ionic=n.Ionic||{},n.Ionic.config=Object.assign({},n.Ionic.config,o),n.Ionic.config;console.error("ionic config was already initialized")}export{setupConfig};