import{h}from "../ionic.core.js";import{c as createColorClasses}from "./chunk-7c632336.js";var Text=function(){function e(){}return e.prototype.hostData=function(){return{class:createColorClasses(this.color)}},e.prototype.render=function(){return h("slot",null)},Object.defineProperty(e,"is",{get:function(){return"ion-text"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"encapsulation",{get:function(){return"shadow"},enumerable:!0,configurable:!0}),Object.defineProperty(e,"properties",{get:function(){return{color:{type:String,attr:"color"},mode:{type:String,attr:"mode"}}},enumerable:!0,configurable:!0}),Object.defineProperty(e,"style",{get:function(){return":host(.ion-color){color:var(--ion-color-base)}"},enumerable:!0,configurable:!0}),e}();export{Text as IonText};