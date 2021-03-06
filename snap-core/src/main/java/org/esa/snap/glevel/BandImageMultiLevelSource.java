/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.snap.glevel;

import com.bc.ceres.core.Assert;
import com.bc.ceres.core.ProgressMonitor;
import com.bc.ceres.glevel.MultiLevelModel;
import com.bc.ceres.glevel.support.AbstractMultiLevelSource;
import com.bc.ceres.glevel.support.DefaultMultiLevelModel;
import org.esa.snap.framework.datamodel.ImageInfo;
import org.esa.snap.framework.datamodel.RasterDataNode;
import org.esa.snap.jai.ImageManager;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;

/**
 * A multi-level source (= level-image source for image pyramids) for visual RGB images derived from
 * {@code RasterDataNode}s.
 *
 * @author Norman Fomferra
 * @since Since BEAM 4.0
 */
public class BandImageMultiLevelSource extends AbstractMultiLevelSource {

    private final RasterDataNode[] rasterDataNodes;
    private ImageInfo imageInfo;

    public static BandImageMultiLevelSource create(RasterDataNode rasterDataNode, ProgressMonitor pm) {
        return create(new RasterDataNode[]{rasterDataNode}, pm);
    }

    public static BandImageMultiLevelSource create(RasterDataNode[] rasterDataNodes, ProgressMonitor pm) {
        RasterDataNode rdn = rasterDataNodes[0];
        MultiLevelModel model = ImageManager.getMultiLevelModel(rdn);
        return create(rasterDataNodes, model, pm);
    }

    public static BandImageMultiLevelSource create(RasterDataNode[] rasterDataNodes, MultiLevelModel model, ProgressMonitor pm) {
        ImageManager.getInstance().prepareImageInfos(rasterDataNodes, pm);
        return new BandImageMultiLevelSource(model, rasterDataNodes);
    }

    private BandImageMultiLevelSource(MultiLevelModel model, RasterDataNode[] rasterDataNodes) {
        super(model);
        this.rasterDataNodes = rasterDataNodes.clone();
        imageInfo = ImageManager.getInstance().getImageInfo(rasterDataNodes);
    }

    public void setImageInfo(ImageInfo imageInfo) {
        this.imageInfo = imageInfo;
    }

    public ImageInfo getImageInfo() {
        return imageInfo;
    }

    @Override
    public Shape getImageShape(int level) {
        return rasterDataNodes[0].getSourceImage().getImageShape(level);
    }

    @Override
    public RenderedImage createImage(int level) {
        return ImageManager.getInstance().createColoredBandImage(rasterDataNodes, imageInfo, level);
    }
}
