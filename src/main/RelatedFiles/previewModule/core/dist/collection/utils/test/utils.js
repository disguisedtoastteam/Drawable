export function generateE2EUrl(component, type, rtl = false) {
    let url = `/src/components/${component}/test/${type}?ionic:_testing=true`;
    if (rtl) {
        url = `${url}&rtl=true`;
    }
    return url;
}
export function cleanScreenshotName(screenshotName) {
    return screenshotName
        .replace(/([-])/g, ' ')
        .replace(/[^0-9a-zA-Z\s]/gi, '')
        .toLowerCase();
}
