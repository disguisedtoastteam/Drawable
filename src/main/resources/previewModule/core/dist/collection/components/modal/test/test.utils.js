import { newE2EPage } from '@stencil/core/testing';
import { cleanScreenshotName, generateE2EUrl } from '../../../utils/test/utils';
export async function testModal(type, selector, rtl = false, screenshotName = cleanScreenshotName(selector)) {
    try {
        const pageUrl = generateE2EUrl('modal', type, rtl);
        if (rtl) {
            screenshotName = `${screenshotName} rtl`;
        }
        const page = await newE2EPage({
            url: pageUrl
        });
        const screenShotCompares = [];
        await page.click(selector);
        await page.waitForSelector(selector);
        let popover = await page.find('ion-modal');
        await popover.waitForVisible();
        screenShotCompares.push(await page.compareScreenshot(screenshotName));
        await popover.callMethod('dismiss');
        await popover.waitForNotVisible();
        screenShotCompares.push(await page.compareScreenshot(`dismiss ${screenshotName}`));
        popover = await page.find('ion-modal');
        expect(popover).toBeNull();
        for (const screenShotCompare of screenShotCompares) {
            expect(screenShotCompare).toMatchScreenshot();
        }
    }
    catch (err) {
        throw err;
    }
}
