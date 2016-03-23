package icy.vtk;

import icy.gui.dialog.IdConfirmDialog;
import icy.gui.dialog.MessageDialog;
import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.system.IcyExceptionHandler;
import icy.system.IcyHandledException;
import icy.system.thread.ThreadUtil;
import icy.util.OpenGLUtil;
import icy.util.ReflectionUtil;

import java.awt.Graphics;
import java.util.concurrent.locks.ReentrantLock;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLJPanel;

import jogamp.opengl.GLDrawableHelper;
import vtk.vtkCamera;
import vtk.vtkGenericOpenGLRenderWindow;
import vtk.vtkGenericRenderWindowInteractor;
import vtk.vtkLight;
import vtk.vtkRenderWindow;
import vtk.vtkRenderWindowInteractor;
import vtk.vtkRenderer;
import vtk.vtkTIFFWriter;
import vtk.vtkWindowToImageFilter;

public class VtkJoglPanel extends GLJPanel
{
    class GLEventImpl implements GLEventListener
    {
        @Override
        public void init(GLAutoDrawable drawable)
        {
            if (!windowset)
            {
                windowset = true;

                // Make sure the JOGL Context is current
                GLContext ctx = drawable.getContext();
                if (!ctx.isCurrent())
                    ctx.makeCurrent();

                // Init VTK OpenGL RenderWindow
                rw.SetMapped(1);
                rw.SetPosition(0, 0);
                setSize(drawable.getWidth(), drawable.getHeight());
                rw.OpenGLInit();

                // init light
                if (!lightingset)
                {
                    lightingset = true;
                    ren.AddLight(lgt);
                    lgt.SetPosition(cam.GetPosition());
                    lgt.SetFocalPoint(cam.GetFocalPoint());
                }
            }
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
        {
            setSize(width, height);
        }

        @Override
        public void display(GLAutoDrawable drawable)
        {
            render();
        }

        @Override
        public void dispose(GLAutoDrawable drawable)
        {
            delete();
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 8821516677188995191L;

    protected vtkGenericOpenGLRenderWindow rw;
    protected vtkRenderer ren;
    protected vtkRenderWindowInteractor wi;
    protected vtkCamera cam;
    protected vtkLight lgt;

    protected ReentrantLock lock;
    protected GLEventImpl glEventImpl;

    protected int lastX;
    protected int lastY;
    protected boolean windowset;
    protected boolean lightingset;
    protected int interactionMode;
    protected boolean rendering;
    private boolean failed;

    public VtkJoglPanel()
    {
        super(new GLCapabilities(GLProfile.getDefault()));

        rw = new vtkGenericOpenGLRenderWindow();

        // init render window
        rw.SetIsDirect(1);
        rw.SetSupportsOpenGL(1);
        rw.SetIsCurrent(true);

        // FIXME: smoothing is broken with VTK 6.3
        // rw.SetPointSmoothing(1);
        // rw.SetLineSmoothing(1);
        // rw.SetPolygonSmoothing(1);
        // rw.SetMultiSamples(4);

        // init window interactor
        wi = new vtkGenericRenderWindowInteractor();
        wi.SetRenderWindow(rw);
        wi.ConfigureEvent();

        ren = new vtkRenderer();
        ren.SetLightFollowCamera(1);

        cam = null;

        lgt = new vtkLight();
        // set ambient color to white
        lgt.SetAmbientColor(1d, 1d, 1d);

        lock = new ReentrantLock();
        glEventImpl = new GLEventImpl();

        windowset = false;
        lightingset = false;
        rendering = false;
        failed = false;

        addGLEventListener(glEventImpl);

        rw.AddRenderer(ren);
        cam = ren.GetActiveCamera();

        // super.setSize(200, 200);
        // rw.SetSize(200, 200);

        // not compatible with OpenGL 3 ? (new VTK OpenGL backend require OpenGL 3.2)
        if (!OpenGLUtil.isOpenGLSupported(3))
        {
            if (!IdConfirmDialog
                    .confirm(
                            "Warning",
                            "Your graphics card driver does not support OpenGL 3, you may experience issues or crashes with VTK.\nDo you want to try anyway ?",
                            IdConfirmDialog.YES_NO_OPTION, getClass().getName() + ".notCompatibleDialog"))
                throw new IcyHandledException("Your graphics card driver is not compatible with OpenGL 3 !");
        }
    }

    /**
     * @deprecated Use {@link #disposeInternal()} instead
     */
    @Deprecated
    public void Delete()
    {
        delete();
    }

    protected void delete()
    {
        if (rendering)
        {
            rw.SetAbortRender(1);
            // wait a bit while rendering
            ThreadUtil.sleep(500);
            // still rendering --> exit
            if (rendering)
                return;
        }

        lock.lock();
        try
        {
            // prevent any further rendering
            rendering = true;

            // if (getParent() != null)
            // getParent().remove(this);

            // release internal VTK objects
            ren = null;
            cam = null;
            lgt = null;

            // On linux we prefer to have a memory leak instead of a crash
            if (!rw.GetClassName().equals("vtkXOpenGLRenderWindow"))
            {
                rw = null;
            }
            else
            {
                System.out.println("The renderwindow has been kept arount to prevent a crash");
            }

            // call it only once in parent as this can take a lot of time
            // vtkObjectBase.JAVA_OBJECT_MANAGER.gc(false);
        }
        finally
        {
            // removing the renderWindow is let to the superclass
            // because in the very special case of an AWT component
            // under Linux, destroying renderWindow crashes.
            lock.unlock();
        }
    }

    /**
     * Disable method, use {@link #disposeInternal()} instead to release VTK and OpenGL resources
     */
    @Override
    protected void dispose()
    {
        // prevent disposal on removeNotify as window externalization produce remove/add operation.
        // --> don't forget to call disposeInternal when needed
    }

    /**
     * Release VTK and OGL objects.<br>
     * Call it when you know you won't use anymore the VTK OGL panel
     */
    public void disposeInternal()
    {
        super.dispose();

        // remove the GL event listener to avoid memory leak
        removeGLEventListener(glEventImpl);

        try
        {
            // hacky fix to avoid the infamous memory leak from ThreadLocal from GLPanel !
            final GLDrawableHelper helper = (GLDrawableHelper) ReflectionUtil.getFieldObject(this, "helper", true);
            final ThreadLocal threadLocal = (ThreadLocal) ReflectionUtil.getFieldObject(helper, "perThreadInitAction",
                    true);
            threadLocal.remove();
        }
        catch (Throwable t)
        {
            // ignore
        }
    }

    /**
     * @deprecated Use {@link #lock()} instead
     */
    @Deprecated
    public void Lock()
    {
        lock();
    }

    /**
     * @deprecated Use {@link #unlock()} instead
     */
    @Deprecated
    public void UnLock()
    {
        unlock();
    }

    /**
     * @deprecated Use {@link #getRenderer()} instead
     */
    @Deprecated
    public vtkRenderer GetRenderer()
    {
        return getRenderer();
    }

    /**
     * @deprecated Use {@link #getRenderWindow()} instead
     */
    @Deprecated
    public vtkRenderWindow GetRenderWindow()
    {
        return getRenderWindow();
    }

    public vtkRenderer getRenderer()
    {
        return ren;
    }

    public vtkRenderWindow getRenderWindow()
    {
        return rw;
    }

    public vtkCamera getCamera()
    {
        return cam;
    }

    public vtkLight getLight()
    {
        return lgt;
    }

    public vtkRenderWindowInteractor getInteractor()
    {
        return wi;
    }

    /**
     * return true if currently rendering
     */
    public boolean isRendering()
    {
        return rendering;
    }

    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);

        if (windowset)
        {
            final int[] size = rw.GetSize();

            // set size only if needed
            if ((size[0] != width) || (size[1] != height))
            {
                lock();
                try
                {
                    wi.SetSize(width, height);
                    rw.SetSize(width, height);
                    sizeChanged();
                }
                finally
                {
                    unlock();
                }
            }
        }
    }

    /**
     * Called when window render size changed (helper for this specific event)
     */
    public void sizeChanged()
    {
        // nothing here but can be overridden
    }

    /**
     * @deprecated Use {@link #render()} instead.
     */
    @Deprecated
    public void Render()
    {
        render();
    }

    /**
     * Do rendering
     */
    public void render()
    {
        if (rendering)
            return;

        rendering = true;
        lock();
        try
        {
            rw.Render();
        }
        finally
        {
            unlock();
            rendering = false;
        }
    }

    // public synchronized void Render()
    // {
    // // already rendering or rendering windows not defined --> exit
    // if ((rendering) || (rw == null))
    // return;
    // // nothing to do --> exit
    // if (ren.VisibleActorCount() == 0)
    // return;
    //
    // rendering = true;
    //
    // try
    // {
    // if (windowset == 0)
    // {
    // // set the window id and the active camera
    // cam = ren.GetActiveCamera();
    //
    // if (lightingset == 0)
    // {
    // ren.AddLight(lgt);
    // lgt.SetPosition(cam.GetPosition());
    // lgt.SetFocalPoint(cam.GetFocalPoint());
    // lightingset = 1;
    // }
    //
    // windowset = 1;
    // setSize(getWidth(), getHeight());
    // }
    //
    // lock();
    // rw.Render();
    // unlock();
    // }
    // finally
    // {
    // rendering = false;
    // }
    // }

    public boolean isWindowSet()
    {
        return windowset;
    }

    public void lock()
    {
        lock.lock();
    }

    public void unlock()
    {
        lock.unlock();
    }

    /**
     * @deprecated do nothing now
     */
    @Deprecated
    public void InteractionModeRotate()
    {
        //
    }

    /**
     * @deprecated do nothing now
     */
    @Deprecated
    public void InteractionModeTranslate()
    {
        //
    }

    /**
     * @deprecated do nothing now
     */
    @Deprecated
    public void InteractionModeZoom()
    {
        //
    }

    /**
     * @deprecated Use {@link #updateLight()} instead
     */
    @Deprecated
    public void UpdateLight()
    {
        updateLight();
    }

    public void updateLight()
    {
        lgt.SetPosition(cam.GetPosition());
        lgt.SetFocalPoint(cam.GetFocalPoint());
    }

    public void resetCameraClippingRange()
    {
        lock();
        try
        {
            ren.ResetCameraClippingRange();
        }
        finally
        {
            unlock();
        }
    }

    public void resetCamera()
    {
        lock();
        try
        {
            ren.ResetCamera();
        }
        finally
        {
            unlock();
        }
    }

    @Override
    public void paint(Graphics g)
    {
        // previous failed --> do nothing now
        if (failed)
            return;

        try
        {
            super.paint(g);
        }
        catch (Throwable t)
        {
            // it can happen with older video cards
            failed = true;

            new FailedAnnounceFrame("An error occured while initializing OpenGL !\n"
                            + "You may try to update your graphics card driver to fix this issue.", 0);

            IcyExceptionHandler.handleException(t, true);
        }
    }

    /**
     * @deprecated Use {@link #doHardCopy(String, int)} instead
     */
    @Deprecated
    public void HardCopy(String filename, int mag)
    {
        doHardCopy(filename, mag);
    }

    public void doHardCopy(String filename, int mag)
    {
        lock();

        vtkWindowToImageFilter w2if = new vtkWindowToImageFilter();
        w2if.SetInput(rw);

        w2if.SetMagnification(mag);
        w2if.Update();

        vtkTIFFWriter writer = new vtkTIFFWriter();
        writer.SetInputConnection(w2if.GetOutputPort());
        writer.SetFileName(filename);
        writer.Write();

        unlock();
    }
}
