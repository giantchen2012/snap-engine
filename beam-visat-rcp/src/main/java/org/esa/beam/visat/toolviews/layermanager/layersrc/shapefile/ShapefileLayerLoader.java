package org.esa.beam.visat.toolviews.layermanager.layersrc.shapefile;

import com.bc.ceres.glayer.Layer;
import org.esa.beam.framework.ui.product.ProductSceneView;
import org.esa.beam.visat.toolviews.layermanager.layersrc.LayerSourcePageContext;

import java.util.concurrent.ExecutionException;

/**
 * todo - add API doc
 *
 * @author Marco Peters
 * @version $ Revision $ Date $
 * @since BEAM 4.6
 */
class ShapefileLayerLoader extends ShapefileLoader {

    ShapefileLayerLoader(LayerSourcePageContext context) {
        super(context);
    }

    @Override
    protected void done() {
        try {
            final Layer layer = get();
            ProductSceneView sceneView = getContext().getAppContext().getSelectedProductSceneView();
            final Layer rootLayer = getContext().getLayerContext().getRootLayer();
            rootLayer.getChildren().add(sceneView.getFirstImageLayerIndex(), layer);
        } catch (InterruptedException ignored) {
        } catch (ExecutionException e) {
            getContext().showErrorDialog("Could not load shape file: \n" + e.getMessage());
            e.printStackTrace();
        }

    }
}
