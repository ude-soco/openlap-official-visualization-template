package org.openlap.examples.svg;

import com.openlap.dataset.OpenLAPDataSet;
import com.openlap.exceptions.UnTransformableData;
import com.openlap.template.DataTransformer;
import com.openlap.template.model.TransformedData;

import java.util.ArrayList;
import java.util.List;

public class SvgDataTransformer implements DataTransformer {
    private TransformedData<List<Point>> transformed;

    @Override
    public TransformedData<List<Point>> transformData(OpenLAPDataSet data) {
        List<?> labels = data.getColumns().get("category").getData();
        List<?> values = data.getColumns().get("value").getData();
        if (labels.size() != values.size()) throw new UnTransformableData("Categories and values must have equal lengths.");
        List<Point> points = new ArrayList<Point>();
        for (int index = 0; index < labels.size(); index++) {
            Object candidate = values.get(index);
            if (!(candidate instanceof Number)) throw new UnTransformableData("Every measure must be a number.");
            double value = ((Number) candidate).doubleValue();
            if (Double.isInfinite(value) || Double.isNaN(value)) throw new UnTransformableData("Every measure must be finite.");
            points.add(new Point(String.valueOf(labels.get(index)), value));
        }
        transformed = new TransformedData<List<Point>>();
        transformed.setData(points);
        return transformed;
    }

    public List<Point> getPoints() { return transformed.getData(); }

    public static final class Point {
        public final String label;
        public final double value;
        Point(String label, double value) { this.label = label; this.value = value; }
    }
}
