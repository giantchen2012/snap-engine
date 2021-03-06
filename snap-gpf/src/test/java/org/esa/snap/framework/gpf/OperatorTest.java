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

package org.esa.snap.framework.gpf;

import com.bc.ceres.core.ProgressMonitor;
import org.esa.snap.framework.datamodel.Band;
import org.esa.snap.framework.datamodel.Product;
import org.esa.snap.framework.datamodel.ProductData;
import org.esa.snap.framework.gpf.annotations.TargetProduct;
import org.junit.Test;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;


public class OperatorTest {

    @Test
    public void testBasicOperatorStatesWithExecute() throws OperatorException, IOException {
        FooExecOp op = new FooExecOp();
        assertNotNull(op.getSpi());
        assertFalse(op.initializeCalled);
        assertFalse(op.runCalled);
        op.execute(ProgressMonitor.NULL);
        assertTrue(op.initializeCalled);
        assertTrue(op.runCalled);
        Product product = op.getTargetProduct();
        assertNotNull(product);
    }

    @Test
    public void testBasicOperatorStatesWithTile() throws OperatorException, IOException {
        FooTileOp op = new FooTileOp();
        assertNotNull(op.getSpi());
        assertFalse(op.initializeCalled);
        assertFalse(op.runCalled);
        assertFalse(op.computeTileCalled);
        Product product = op.getTargetProduct();
        assertTrue(op.initializeCalled);
        assertFalse(op.runCalled);
        assertFalse(op.computeTileCalled);
        assertNotNull(product);
        assertFalse(product.isModified());
        product.getBand("bar").readRasterDataFully(ProgressMonitor.NULL);
        assertTrue(op.initializeCalled);
        assertTrue(op.runCalled);
        assertTrue(op.computeTileCalled);
    }

    @Test
    public void testBasicOperatorStatesWithTileStack() throws OperatorException, IOException {
        FooTileStackOp op = new FooTileStackOp();
        assertNotNull(op.getSpi());
        assertFalse(op.initializeCalled);
        assertFalse(op.runCalled);
        assertFalse(op.computeTileStackCalled);
        Product product = op.getTargetProduct();
        assertTrue(op.initializeCalled);
        assertFalse(op.runCalled);
        assertFalse(op.computeTileStackCalled);
        assertNotNull(product);
        assertFalse(product.isModified());
        product.getBand("bar").readRasterDataFully(ProgressMonitor.NULL);
        assertTrue(op.initializeCalled);
        assertTrue(op.runCalled);
        assertTrue(op.computeTileStackCalled);
    }

    @Test
    public void testThatGetTargetProductMustNotBeCalledFromInitialize() {
        Operator op = new Operator() {
            @Override
            public void initialize() throws OperatorException {
                getTargetProduct();
            }
        };
        try {
            op.getTargetProduct();
            fail("RuntimeException expected: Operator shall not allow calling getTargetProduct() from within initialize().");
        } catch (OperatorException ignored) {
            fail("RuntimeException expected: Operator shall not allow calling getTargetProduct() from within initialize().");
        } catch (RuntimeException ignored) {
            // ok, passed
        }
    }

    @Test
    public void testSourceProducts() throws IOException, OperatorException {
        final Operator operator = new Operator() {
            @Override
            public void initialize() throws OperatorException {
            }
        };

        final Product sp1 = new Product("sp1", "t", 1, 1);
        final Product sp2 = new Product("sp2", "t", 1, 1);
        final Product sp3 = new Product("sp3", "t", 1, 1);

        operator.setSourceProduct(sp1);
        assertSame(sp1, operator.getSourceProduct());
        assertSame(sp1, operator.getSourceProduct("sourceProduct"));

        operator.setSourceProduct("sp1", sp1);
        assertSame(sp1, operator.getSourceProduct());
        assertSame(sp1, operator.getSourceProduct("sourceProduct"));
        assertSame(sp1, operator.getSourceProduct("sp1"));

        Product[] products = operator.getSourceProducts();
        assertNotNull(products);
        assertEquals(1, products.length);
        assertSame(sp1, products[0]);

        operator.setSourceProduct("sp2", sp2);
        products = operator.getSourceProducts();
        assertNotNull(products);
        assertEquals(2, products.length);
        assertSame(sp1, products[0]);
        assertSame(sp2, products[1]);

        operator.setSourceProducts(new Product[]{sp3, sp2, sp1});
        assertNull(operator.getSourceProduct("sourceProduct"));
        assertNull(operator.getSourceProduct("sp1"));
        assertNull(operator.getSourceProduct("sp2"));
        products = operator.getSourceProducts();
        assertNotNull(products);
        assertEquals(3, products.length);
        assertSame(sp3, products[0]);
        assertSame(sp2, products[1]);
        assertSame(sp1, products[2]);
        assertSame(sp3, operator.getSourceProduct("sourceProduct.1"));
        assertSame(sp3, operator.getSourceProduct("sourceProduct1"));
        assertSame(sp2, operator.getSourceProduct("sourceProduct.2"));
        assertSame(sp2, operator.getSourceProduct("sourceProduct2"));
        assertSame(sp1, operator.getSourceProduct("sourceProduct.3"));
        assertSame(sp1, operator.getSourceProduct("sourceProduct3"));
        assertEquals("sourceProduct.3", operator.getSourceProductId(sp1));
        assertEquals("sourceProduct.2", operator.getSourceProductId(sp2));
        assertEquals("sourceProduct.1", operator.getSourceProductId(sp3));


        operator.setSourceProducts(new Product[]{sp1, sp2, sp1});
        products = operator.getSourceProducts();
        assertNotNull(products);
        assertEquals(2, products.length);
        assertSame(sp1, products[0]);
        assertSame(sp2, products[1]);
        assertSame(sp1, operator.getSourceProduct("sourceProduct.1"));
        assertSame(sp1, operator.getSourceProduct("sourceProduct1"));
        assertSame(sp2, operator.getSourceProduct("sourceProduct.2"));
        assertSame(sp2, operator.getSourceProduct("sourceProduct2"));
        assertSame(sp1, operator.getSourceProduct("sourceProduct.3"));
        assertSame(sp1, operator.getSourceProduct("sourceProduct3"));
        assertEquals("sourceProduct.1", operator.getSourceProductId(sp1));
        assertEquals("sourceProduct.2", operator.getSourceProductId(sp2));
        assertNull(operator.getSourceProductId(sp3));
    }

    private static Product createFooProduct() {
        Product product = new Product("foo", "grunt", 1, 1);
        product.addBand("bar", ProductData.TYPE_FLOAT64);
        return product;
    }

    private static class FooExecOp extends Operator {

        private boolean initializeCalled;
        private boolean runCalled;
        @TargetProduct
        private Product targetProduct;

        @Override
        public void initialize() throws OperatorException {
            initializeCalled = true;
            targetProduct = createFooProduct();
            targetProduct.addBand("foo", ProductData.TYPE_INT8); // will set the "modified" flag
        }

        @Override
        public void doExecute(ProgressMonitor pm) {
            runCalled = true;
        }
    }

    private static class FooTileOp extends Operator {

        private boolean initializeCalled;
        private boolean runCalled;
        private boolean computeTileCalled;
        @TargetProduct
        private Product targetProduct;

        @Override
        public void initialize() throws OperatorException {
            initializeCalled = true;
            targetProduct = createFooProduct();
            targetProduct.addBand("foo", ProductData.TYPE_INT8); // will set the "modified" flag
        }

        @Override
        public void doExecute(ProgressMonitor pm) {
            runCalled = true;
        }

        @Override
        public void computeTile(Band band, Tile tile, ProgressMonitor pm) throws OperatorException {
            computeTileCalled = true;
        }
    }

    private static class FooTileStackOp extends Operator {

        private boolean initializeCalled;
        private boolean runCalled;
        private boolean computeTileStackCalled;
        @TargetProduct
        private Product targetProduct;

        @Override
        public void initialize() throws OperatorException {
            initializeCalled = true;
            targetProduct = createFooProduct();
            targetProduct.addBand("foo", ProductData.TYPE_INT8); // will set the "modified" flag
        }

        @Override
        public void doExecute(ProgressMonitor pm) {
            runCalled = true;
        }


        @Override
        public void computeTileStack(Map<Band, Tile> targetTiles, Rectangle targetRectangle, ProgressMonitor pm) throws OperatorException {
            computeTileStackCalled = true;
        }
    }
}
