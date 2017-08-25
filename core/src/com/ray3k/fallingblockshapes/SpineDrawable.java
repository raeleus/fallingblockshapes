/*
 * The MIT License
 *
 * Copyright 2017 Raymond Buckley.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ray3k.fallingblockshapes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Bone;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonRenderer;

public class SpineDrawable extends BaseDrawable {
    private final Skeleton skeleton;
    private final AnimationState animationState;
    private final SkeletonRenderer skeletonRenderer;
    private final Array<Bone> widthBones;
    private final Array<Bone> heightBones;

    public SpineDrawable(SkeletonData skeletonData, SkeletonRenderer skeletonRenderer) {
        widthBones = new Array<Bone>();
        heightBones = new Array<Bone>();
        this.skeletonRenderer = skeletonRenderer;
        
        skeleton = new Skeleton(skeletonData);
        AnimationStateData animationStateData = new AnimationStateData(skeletonData);
        animationState = new AnimationState(animationStateData);
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public AnimationState getAnimationState() {
        return animationState;
    }
    
    public AnimationStateData getAnimationStateData() {
        return animationState.getData();
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        for (Bone bone : widthBones) {
            bone.setScaleX(width);
        }
        
        for (Bone bone : heightBones) {
            bone.setScaleY(height);
        }
        
        skeleton.setPosition(x, y);
        animationState.update(Gdx.graphics.getDeltaTime());
        skeleton.updateWorldTransform();
        animationState.apply(skeleton);
        
        skeletonRenderer.draw(batch, skeleton);
    }
}
