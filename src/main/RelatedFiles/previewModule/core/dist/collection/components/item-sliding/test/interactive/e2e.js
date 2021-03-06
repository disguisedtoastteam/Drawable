import { newE2EPage } from '@stencil/core/testing';
test('item-sliding: interactive', async () => {
    const page = await newE2EPage({
        url: '/src/components/item-sliding/test/interactive?ionic:_testing=true'
    });
    const compare = await page.compareScreenshot();
    expect(compare).toMatchScreenshot();
    const items = await page.$$('ion-item-sliding');
    expect(items.length).toEqual(3);
    await slideAndDelete(items[0], page);
    const itemsAfterFirstSlide = await page.$$('ion-item-sliding');
    expect(itemsAfterFirstSlide.length).toEqual(2);
    await slideAndDelete(items[1], page);
    const itemsAfterSecondSlide = await page.$$('ion-item-sliding');
    expect(itemsAfterSecondSlide.length).toEqual(1);
});
async function slideAndDelete(item, page) {
    try {
        const id = await (await item.getProperty('id')).jsonValue();
        const boundingBox = await item.boundingBox();
        const centerX = parseFloat(boundingBox.x + boundingBox.width / 2);
        const centerY = parseFloat(boundingBox.y + boundingBox.height / 2);
        await page.mouse.move(centerX, centerY);
        await page.mouse.down();
        await page.mouse.move(0, centerY);
        await page.mouse.up();
        const options = await item.$$('ion-item-option');
        await options[0].click();
        await page.waitForSelector(id, { hidden: true });
    }
    catch (err) {
        throw err;
    }
}
