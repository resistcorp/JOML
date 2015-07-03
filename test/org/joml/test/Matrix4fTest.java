package org.joml.test;

import java.nio.IntBuffer;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Tests for the {@link Matrix4f} class.
 * 
 * @author Kai Burjack
 */
public class Matrix4fTest extends TestCase {

    /**
     * Test that project and unproject are each other's inverse operations.
     */
    public void testProjectUnproject() {
        /* Build some arbitrary viewport. */
        IntBuffer viewport = IntBuffer.wrap(new int[]{0, 0, 800, 800});

        Vector3f expected = new Vector3f(1.0f, 2.0f, -3.0f);
        Vector3f actual = new Vector3f();
        Matrix4f inverse = new Matrix4f();

        /* Build a perspective projection and then project and unproject. */
        new Matrix4f()
        .perspective(45.0f, 1.0f, 0.01f, 100.0f)
        .project(expected, viewport, actual)
        .unproject(actual, viewport, inverse, actual);

        /* Check for equality of the components */
        assertEquals(expected.x, actual.x, TestUtil.MANY_OPS_AROUND_ZERO_PRECISION_FLOAT);
        assertEquals(expected.y, actual.y, TestUtil.MANY_OPS_AROUND_ZERO_PRECISION_FLOAT);
        assertEquals(expected.z, actual.z, TestUtil.MANY_OPS_AROUND_ZERO_PRECISION_FLOAT);
    }

    public void testLookAt() {
        {
        Matrix4f m1 = new Matrix4f().lookAt(0.0f, 2.0f, 3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        Matrix4f m2 = new Matrix4f().translate(0.0f, 0.0f, -(float) Math.sqrt(2*2 + 3*3)).rotateX((float) Math.toDegrees(Math.atan2(2, 3)));
        TestUtil.assertMatrix4fEquals(m1, m2, 1E-15f);
        }
        {
        Matrix4f m1 = new Matrix4f().lookAt(3.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        Matrix4f m2 = new Matrix4f().translate(0.0f, 0.0f, -(float) Math.sqrt(2*2 + 3*3)).rotateX((float) Math.toDegrees(Math.atan2(2, 3))).rotateY(-90.0f);
        TestUtil.assertMatrix4fEquals(m1, m2, 1E-15f);
        }
    }

    /**
     * Test computing the frustum planes with a combined view-projection matrix with translation.
     */
    public void testFrustumPlanePerspectiveRotateTranslate() {
        /* Move the camera 5 units "up" and rotate it clock-wise 90 degrees around Y */
        Vector4f left = new Vector4f();
        Vector4f right = new Vector4f();
        Vector4f top = new Vector4f();
        Vector4f bottom = new Vector4f();
        Vector4f near = new Vector4f();
        Vector4f far = new Vector4f();
        new Matrix4f()
        .perspective(90.0f, 1.0f, 0.1f, 100.0f)
        .rotateY(90)
        .translate(0, -5, 0)
            .frustumPlane(Matrix4f.PLANE_LEFT, left)
            .frustumPlane(Matrix4f.PLANE_RIGHT, right)
            .frustumPlane(Matrix4f.PLANE_BOTTOM, bottom)
            .frustumPlane(Matrix4f.PLANE_TOP, top)
            .frustumPlane(Matrix4f.PLANE_NEAR, near)
            .frustumPlane(Matrix4f.PLANE_FAR, far);

        Vector4f expectedLeft = new Vector4f(1, 0, 1, 0).normalize3();
        Vector4f expectedRight = new Vector4f(1, 0, -1, 0).normalize3();
        Vector4f expectedTop = new Vector4f(1, -1, 0, 5).normalize3();
        Vector4f expectedBottom = new Vector4f(1, 1, 0, -5).normalize3();
        Vector4f expectedNear = new Vector4f(1, 0, 0, -0.1f).normalize3();
        Vector4f expectedFar = new Vector4f(-1, 0, 0, 100.0f).normalize3();

        TestUtil.assertVector4fEquals(expectedLeft, left, 1E-5f);
        TestUtil.assertVector4fEquals(expectedRight, right, 1E-5f);
        TestUtil.assertVector4fEquals(expectedTop, top, 1E-5f);
        TestUtil.assertVector4fEquals(expectedBottom, bottom, 1E-5f);
        TestUtil.assertVector4fEquals(expectedNear, near, 1E-5f);
        TestUtil.assertVector4fEquals(expectedFar, far, 1E-4f);
    }

    public void testIsPointInFrustumPlanePerspectiveRotate() {
        Matrix4f m = new Matrix4f().perspective(90.0f, 1.0f, 0.1f, 100.0f)
                .rotateY(90);
        Assert.assertTrue(m.isPointInsideFrustum(50, 0, 0));
        Assert.assertFalse(m.isPointInsideFrustum(50, 51, 0));
    }

    public void testIsSphereInFrustumPlaneOrtho() {
        Matrix4f m = new Matrix4f().ortho(-1, 1, -1, 1, -1, 1);
        Assert.assertTrue(m.isSphereInsideFrustum(1, 0, 0, 0.1f));
        Assert.assertFalse(m.isSphereInsideFrustum(1.2f, 0, 0, 0.1f));
    }

    public void testIsAabInFrustumPlaneOrtho() {
        Matrix4f m = new Matrix4f().ortho(-1, 1, -1, 1, -1, 1);
        for (int i = 0; i < 500 * 500; i++)
        Assert.assertTrue(m.isAabInsideFrustum(0, 0, 0, 2, 2, 2));
        for (int i = 0; i < 500 * 500; i++)
        Assert.assertFalse(m.isAabInsideFrustum(1.1f, 0, 0, 2, 2, 2));
    }

}
