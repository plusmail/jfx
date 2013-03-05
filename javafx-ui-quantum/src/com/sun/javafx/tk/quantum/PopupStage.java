/*
 * Copyright (c) 2008, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sun.javafx.tk.quantum;

import javafx.stage.StageStyle;

import com.sun.glass.ui.Application;
import com.sun.glass.ui.Screen;
import com.sun.glass.ui.Window;
import com.sun.javafx.tk.TKScene;
import com.sun.prism.impl.PrismSettings;

class PopupStage extends WindowStage  {

    private GlassStage ownerStage;

    public PopupStage(boolean verbose, final Object owner) {
        super(verbose, StageStyle.TRANSPARENT);
        assert owner instanceof GlassStage;
        ownerStage = (GlassStage)owner;
    }

    @Override
    protected void initPlatformWindow() {
        Application app = Application.GetApplication();
        Window owner = (ownerStage instanceof WindowStage) ?
                       ((WindowStage)ownerStage).getPlatformWindow() :
                       null;
        platformWindow = app.createWindow(owner, Screen.getMainScreen(),
                                          Window.TRANSPARENT | Window.POPUP);
        platformWindow.setFocusable(false);
    }

    public GlassScene getOwnerScene() {
        return ownerStage.scene;
    }

    @Override
    public TKScene createTKScene(boolean depthBuffer) {
        PopupScene scene = new PopupScene(verbose, depthBuffer);
        scene.setGlassStage(this);

        return scene;
    }

    public void setResizable(final boolean resizable) {
    }

    public void setTitle(final String title) {
    }

    public void sizeToScene() {
    }

    public void centerOnScreen(){
    }

    public void close() {
        super.close();
    }

    @Override
    public void setBounds(float x, float y, boolean xSet, boolean ySet,
                          float width, float height, float clientWidth, float clientHeight,
                          float xGravity, float yGravity)
    {
        super.setBounds(x, y, xSet, ySet, width, height, clientWidth, clientHeight, xGravity, yGravity);
    }

    @Override public void setVisible(boolean visible) {
        if (visible) {
            ownerStage.addPopup(this);
        } else {
            ownerStage.removePopup(this);
        }
        super.setVisible(visible);
    }

    @Override public boolean isTopLevel() {
        return false;
    }

}
