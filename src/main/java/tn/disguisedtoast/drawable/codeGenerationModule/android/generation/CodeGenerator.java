package tn.disguisedtoast.drawable.codeGenerationModule.android.generation;

import tn.disguisedtoast.drawable.codeGenerationModule.android.models.DetectedObject;
import tn.disguisedtoast.drawable.codeGenerationModule.android.models.views.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {

    public static ParseResult parse(List<DetectedObject> detectedObjects) {
        if (detectedObjects == null || detectedObjects.isEmpty()) {
            return null;
        }
        //find frame and remove it from list
        List<DetectedObject> frames = new ArrayList<>();
        List<DetectedObject> objects = new ArrayList<>();
        for (DetectedObject object : detectedObjects) {
            if (object.getClasse() == DetectedObject.FRAME) {
                frames.add(object);
            } else {
                objects.add(object);
            }
        }
        if (frames.isEmpty() || objects.isEmpty()) {
            return null;
        }

        DetectedObject topFrame = frames.get(0);
        DetectedObject bottomFrame = frames.get(0);
        for (DetectedObject object : frames) {
            if (object.getBox().getyMin() < topFrame.getBox().getyMin()) {
                topFrame = object;
            }
            if (object.getBox().getyMin() > bottomFrame.getBox().getyMin()) {
                bottomFrame = object;
            }
        }
        if (topFrame == bottomFrame) {
            return parseWithSingleFrame(topFrame, objects);
        } else {
            return parseWithDoubleFrames(topFrame, bottomFrame, objects);
        }
    }

    private static ParseResult parseWithDoubleFrames(DetectedObject topFrame, DetectedObject bottomFrame, List<DetectedObject> objects) {
        List<View> views = new ArrayList<>();
        int i = 1;
        int j = 1;
        int k = 1;
        for (DetectedObject object : objects) {
            View view = getViewInstance(object);
            if (view == null) continue;
            view.setId(view.getClass().getSimpleName() + i);
            if (view instanceof Button) {
                ((Button) view).setText("Button " + j);
                j++;
            }
            if (view instanceof EditText) {
                ((EditText) view).setHint("EditText " + k);
                k++;
            }
            view.setTopToTop("parent");
            view.setBottomToBottom("parent");
            view.setRightToRight("parent");
            view.setLeftToLeft("parent");
            view.setWidth("0dp");
            if (view instanceof ImageView) {
                view.setHeight("0dp");
                double heightPercent = object.getBox().getHeight() / (bottomFrame.getBox().getyMin() - topFrame.getBox().getyMax());
                view.setHeightPercent(heightPercent > 1 ? "1" : heightPercent + "");
            } else {
                view.setHeight("wrap_content");
            }

            double verticalBias = Math.min(1, Math.max(0, object.getBox().getyMin() - topFrame.getBox().getyMax()) /
                    Math.max(0, bottomFrame.getBox().getyMin() - topFrame.getBox().getyMax() - object.getBox().getHeight()));
            double horizontalBias = Math.max(0, object.getBox().getxMin() - topFrame.getBox().getxMin()) /
                    Math.max(0, topFrame.getBox().getWidth() - object.getBox().getWidth());

            view.setVerticalBias(verticalBias + "");
            view.setHorizontalBias(horizontalBias + "");

            view.setWidthPercent(object.getBox().getWidth() / topFrame.getBox().getWidth() > 1 ? "1" : object.getBox().getWidth() / topFrame.getBox().getWidth() + "");

            views.add(view);
            i++;
        }

        return new ParseResult(false, views, null, buildLayout(views));
    }

    private static ParseResult parseWithSingleFrame(DetectedObject topFrame, List<DetectedObject> objects) {

        //order by y ascending
        objects.sort((a, b) -> {
            if (a.getBox().getyMin() - b.getBox().getyMin() > 0) {
                return 1;
            }
            if (a.getBox().getyMin() - b.getBox().getyMin() < 0) {
                return -1;
            }
            return 0;
        });

        //group horizontally adjacent objects
        List<List<ObjectWrapper>> grid = new ArrayList<>();
        List<ObjectWrapper> neighbors = new ArrayList<>();
        grid.add(neighbors);
        int i = 1;
        int j = 1;
        int k = 1;
        for (DetectedObject object : objects) {
            if (neighbors.isEmpty() || areNeighbors(object, neighbors.get(0).detectedObject)) {
                View view = getViewInstance(object);
                if (view == null) continue;
                view.setId(view.getClass().getSimpleName() + i);
                if (view instanceof Button) {
                    ((Button) view).setText("Button " + j);
                    j++;
                }
                if (view instanceof EditText) {
                    ((EditText) view).setHint("EditText " + k);
                    k++;
                }
                neighbors.add(new ObjectWrapper(view, object));
            } else {
                neighbors = new ArrayList<>();
                View view = getViewInstance(object);
                if (view == null) continue;
                view.setId(view.getClass().getSimpleName() + i);
                if (view instanceof Button) {
                    ((Button) view).setText("Button " + j);
                    j++;
                }
                if (view instanceof EditText) {
                    ((EditText) view).setHint("EditText " + k);
                    k++;
                }
                neighbors.add(new ObjectWrapper(view, object));
                grid.add(neighbors);
            }
            i++;
        }

        //sort adjacent objects by x
        for (List<ObjectWrapper> list : grid) {
            list.sort((a, b) -> {
                if (a.detectedObject.getBox().getxMin() - b.detectedObject.getBox().getxMin() > 0) {
                    return 1;
                }
                if (a.detectedObject.getBox().getxMin() - b.detectedObject.getBox().getxMin() < 0) {
                    return -1;
                }
                return 0;
            });
        }

        List<View> views = new ArrayList<>();
        //TODO: consider element being in the center not to the left of parent
        //set size, constraints, and content
        for (i = 0; i < grid.size(); i++) {
            List<ObjectWrapper> list = grid.get(i);
            if (i == 0) { //if this is the top left element in the screen
                list.get(0).view.setTopToTop("parent");
            } else {
                ObjectWrapper elementToSetTopConstraintsTo = grid.get(i - 1).get(0);
                for (ObjectWrapper wrapper : grid.get(i - 1)) {
                    if (wrapper.detectedObject.getBox().getyMax() > elementToSetTopConstraintsTo.detectedObject.getBox().getyMax()) {
                        elementToSetTopConstraintsTo = wrapper;
                    }
                }
                list.get(0).view.setTopToBottom("@id/" + elementToSetTopConstraintsTo.view.getSimpleId());
            }
            list.get(0).view.setLeftToLeft("parent");
            list.get(0).view.setMarginTop("8dp");
            list.get(0).view.setWidth("0dp");
            list.get(0).view.setWidthPercent(list.get(0).detectedObject.getBox().getWidth() / topFrame.getBox().getWidth() + "");
            list.get(0).view.setHeight("wrap_content");

            list.get(0).view.setRightToRight("parent");
            list.get(0).view.setHorizontalBias(
                    Math.max(0, list.get(0).detectedObject.getBox().getxMin() - topFrame.getBox().getxMin())
                            /
                            Math.max(0, topFrame.getBox().getWidth() - list.get(0).detectedObject.getBox().getWidth())
                            + ""
            );//if the objected is to the left of frame, count that as 0 margin

            views.add(list.get(0).view);

            for (j = 1; j < list.size(); j++) {
                list.get(j).view.setTopToTop("@id/" + list.get(0).view.getSimpleId());
                list.get(j).view.setLeftToRight("@id/" + list.get(j - 1).view.getSimpleId());
                list.get(j).view.setWidth("0dp");
                list.get(j).view.setWidthPercent(list.get(j).detectedObject.getBox().getWidth() / topFrame.getBox().getWidth() + "");
                list.get(j).view.setHeight("wrap_content");

                list.get(j).view.setRightToRight("parent");
                list.get(j).view.setHorizontalBias(
                        (
                                list.get(j).detectedObject.getBox().getxMin()
                                        -
                                        list.get(j - 1).detectedObject.getBox().getxMax()
                        )
                                /
                                (
                                        (topFrame.getBox().getWidth() - list.get(j - 1).detectedObject.getBox().getxMax())
                                                -
                                                list.get(j).detectedObject.getBox().getWidth()
                                )
                                + ""
                );

                views.add(list.get(j).view);
            }
        }

        return new ParseResult(true, views, grid, buildLayout(views));
    }

    private static ConstraintLayout buildLayout(List<View> views) {
        ConstraintLayout layout = new ConstraintLayout();
        layout.setWidth("match_parent");
        layout.setHeight("match_parent");

        for (View view : views) {
            if (view instanceof Button) {
                if (layout.getButtons() == null) {
                    layout.setButtons(new ArrayList<>());
                }
                layout.getButtons().add((Button) view);
            }
            if (view instanceof ImageView) {
                if (layout.getImageViews() == null) {
                    layout.setImageViews(new ArrayList<>());
                }
                layout.getImageViews().add((ImageView) view);
            }
            if (view instanceof EditText) {
                if (layout.getEditTexts() == null) {
                    layout.setEditTexts(new ArrayList<>());
                }
                layout.getEditTexts().add((EditText) view);
            }
        }
        return layout;
    }

    public static void generateLayoutFile(ConstraintLayout layout) throws JAXBException, FileNotFoundException {
        if (layout == null) {
            System.out.println("layout is null");
            return;
        }
        JAXBContext jc = JAXBContext.newInstance(ConstraintLayout.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        //marshaller.marshal(layout, System.out);
        OutputStream os = new FileOutputStream("../AndroidTest/app/src/main/res/layout/layout.xml");
        marshaller.marshal(layout, os);
    }

    private static View getViewInstance(DetectedObject object) {
        switch ((int) object.getClasse()) {
            case DetectedObject.BUTTON:
                return new Button();
            case DetectedObject.IMAGE:
                return new ImageView();
            case DetectedObject.EditText:
                return new EditText();
        }
        return null;
    }

    private static boolean areNeighbors(DetectedObject o1, DetectedObject o2) {
        //two elements are neighbors if the distance between their centers is less or equal to the greater height of the two divided by two
        return Math.abs(o1.getBox().getyMin() + o1.getBox().getHeight() / 2 - o2.getBox().getyMin() - o2.getBox().getHeight() / 2) <=
                Math.max(o1.getBox().getHeight() / 2, o2.getBox().getHeight() / 2);
    }

    public static class ObjectWrapper {
        View view;
        DetectedObject detectedObject;

        public ObjectWrapper() {

        }

        ObjectWrapper(View view, DetectedObject detectedObject) {
            this.view = view;
            this.detectedObject = detectedObject;
        }

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }

        public DetectedObject getDetectedObject() {
            return detectedObject;
        }

        public void setDetectedObject(DetectedObject detectedObject) {
            this.detectedObject = detectedObject;
        }

        @Override
        public String toString() {
            return "ObjectWrapper{" +
                    "views=" + view +
                    "}\n";
        }
    }

    public static class ParseResult {
        boolean hasSingleFrame;
        List<View> views;
        List<List<ObjectWrapper>> rows;
        ConstraintLayout layout;

        public ParseResult() {
        }

        public ParseResult(boolean hasSingleFrame, List<View> views, List<List<ObjectWrapper>> rows, ConstraintLayout layout) {
            this.hasSingleFrame = hasSingleFrame;
            this.views = views;
            this.rows = rows;
            this.layout = layout;
        }

        public boolean hasSingleFrame() {
            return hasSingleFrame;
        }

        public void setHasSingleFrame(boolean hasSingleFrame) {
            this.hasSingleFrame = hasSingleFrame;
        }

        public List<View> getViews() {
            return views;
        }

        public void setViews(List<View> views) {
            this.views = views;
        }

        public List<List<ObjectWrapper>> getRows() {
            return rows;
        }

        public void setRows(List<List<ObjectWrapper>> rows) {
            this.rows = rows;
        }

        public ConstraintLayout getLayout() {
            return layout;
        }

        public void setLayout(ConstraintLayout layout) {
            this.layout = layout;
        }
    }
}
