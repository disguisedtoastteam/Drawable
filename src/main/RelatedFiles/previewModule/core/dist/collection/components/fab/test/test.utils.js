import { newE2EPage } from '@stencil/core/testing';
import { cleanScreenshotName, generateE2EUrl } from '../../../utils/test/utils';
export async function testFab(type, selector, rtl = false, screenshotName = cleanScreenshotName(selector)) {
    try {
        const pageUrl = generateE2EUrl('fab', type, rtl);
        if (rtl) {
            screenshotName = `${screenshotName} rtl`;
        }
        const page = await newE2EPage({
            url: pageUrl
        });
        const screenshotCompares = [];
        screenshotCompares.push(await page.compareScreenshot(`${screenshotName}`));
        const fab = await getFabComponent(page, selector);
        await fab.click();
        await ensureFabState(fab, 'active');
        screenshotCompares.push(await page.compareScreenshot(`${screenshotName} open`));
        const fabButton = await getFabButton(fab);
        await fabButton.click();
        await ensureFabState(fab, 'inactive');
        screenshotCompares.push(await page.compareScreenshot(`${screenshotName} close`));
        for (const screenShotCompare of screenshotCompares) {
            expect(screenShotCompare).toMatchScreenshot();
        }
    }
    catch (err) {
        throw err;
    }
}
export async function testDisabledFab(type, selector, rtl = false, screenshotName = cleanScreenshotName(selector)) {
    try {
        const pageUrl = generateE2EUrl('fab', type, rtl);
        if (rtl) {
            screenshotName = `${screenshotName} rtl`;
        }
        const page = await newE2EPage({
            url: pageUrl
        });
        const screenshotCompares = [];
        screenshotCompares.push(await page.compareScreenshot(`disabled ${screenshotName}`));
        const fab = await getFabComponent(page, selector);
        await fab.click();
        await ensureFabState(fab, 'inactive');
        screenshotCompares.push(await page.compareScreenshot(`disabled ${screenshotName} attempt open`));
        for (const screenShotCompare of screenshotCompares) {
            expect(screenShotCompare).toMatchScreenshot();
        }
    }
    catch (err) {
        throw err;
    }
}
async function getFabComponent(page, selector) {
    return page.find(selector);
}
async function getFabButton(fabComponent) {
    return fabComponent.find('ion-fab-button');
}
async function getFabList(fabComponent) {
    return fabComponent.find('ion-fab-list');
}
async function ensureFabState(fab, state) {
    const active = (state === 'active') ? true : false;
    const fabList = await getFabList(fab);
    expect(fabList.classList.contains('fab-list-active')).toBe(active);
}
