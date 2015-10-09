package icy.sequence;

import icy.image.IcyBufferedImage;
import icy.type.DataType;
import icy.type.collection.array.Array1DUtil;
import icy.type.collection.array.Array2DUtil;

/**
 * This class is intended for plugins that construct new sequences by:<br>
 *  - first, specifying a data-type and a 5D-size for the sequence to be created<br>
 *  - second, visiting each sample (x,y,z,t,c) to set its value.<br>
 * <br>
 * A typical example of such situation would be a plugin that take a sequence
 * in input and return a copy of that sequence in output.
 * 
 * For such plugins, the SequenceBuilder class provides some tools to ease the
 * creation of the output sequence, especially in a multi-thread context. To
 * cut a long story short, here is an example of how the above-mentioned copy
 * algorithm could be implemented with SequenceBuilder:<br>
 * <br>
 *  <pre>
 *   // Input sequence
 *   Sequence in = ...
 *   
 *   // Create a SequenceBuilder object, that will be in charge of allocating
 *   // and feeding the output sequence.
 *   SequenceBuilder builder = new SequenceBuilder
 *   (
 *     // The output sequence will have the same size and data-type than the input
 *     in.getSizeX(), in.getSizeY(), in.getSizeZ(), in.getSizeT(), in.getSizeC(),
 *     in.getDataType_()
 *   );
 *   
 *   // Start the building process.
 *   builder.beginUpdate();
 *   try
 *   {
 *     // Here, the copy can be multi-threaded. No synchronization or mutex-locking
 *     // is required on the side of the SequenceBuilder object.
 *     
 *     ...
 *     
 *     // Thread 1
 *     forall(int t,z,c in listOfXYPlanesToBeDoneByThread1)
 *     {
 *       // Retrieve the array that will hold the pixel values corresponding to
 *       // the XY-plane at coordinates (t, z, c) in the output sequence. If this
 *       // object does not exist, it is created. The object returned by the
 *       // method getData is actually an instance of byte[], short[], int[],
 *       // float[] or double[], depending on the data-type specified in the
 *       // SequenceBuilder constructor.
 *       // 
 *       // Remark: depending on the context, it may be more convenient to call
 *       // builder.getDataAsDouble, builder.getDataAsByte, etc...
 *       // 
 *       Object buffer = builder.getData(t, z, c);
 *       
 *       // Execute the copy. For example:
 *       { Array1DUtil.arrayToArray(in.getDataXY(t, z, c), buffer); }
 *       
 *       // Mark the array returned by the previous call to builder.getData(t, z, c)
 *       // as ready to be incorporated in the output sequence.
 *       // 
 *       // Remark: there should only be one call to the method validateData
 *       // per set of coordinates (t, z, c).
 *       //
 *       builder.validateData(t, z, c);
 *     }
 *     
 *     ...
 *     
 *     // Thread 2
 *     forall(int t,z,c in listOfXYPlanesToBeDoneByThread2) {
 *       // etc...
 *     }
 *     
 *     ...
 *     
 *   }
 *   
 *   // Finish the building process (each call to beginUpdate() must be followed
 *   // by a call to endUpdate()).
 *   finally {
 *     builder.endUpdate();
 *   }
 *   
 *   // Return the sequence that have been created by the SequenceBuilder object.
 *   return builder.getResult();
 * </pre>
 * 
 * @author Yoann Le Montagner
 */
public class SequenceBuilder
{
	private int               _sizeX    ;
	private int               _sizeY    ;
	private int               _sizeZ    ;
	private int               _sizeT    ;
	private int               _sizeC    ;
	private DataType          _dataType ;
	private Sequence          _result   ;
	private SequenceAllocator _allocator;
	
	/**
	 * Allocate a new sequence that will have the given size, dataType and an empty name
	 */
	public SequenceBuilder(int sizeX, int sizeY, int sizeZ, int sizeT, int sizeC, DataType dataType)
	{
		this(sizeX, sizeY, sizeZ, sizeT, sizeC, dataType, null);
	}
	
	/**
	 * If non-null, the 'target' argument will be used to store the result of the
	 * sequence building process, and no new sequence will be created.<br>
	 * <br>
	 * Two situations may occur:
	 * <li>The sequence 'target' has the same size and data-type than specified by
	 * the arguments passed to the SequenceBuilder object. In that case, no new
	 * buffer/image allocation is performed, and the sequence is only modified
	 * through calls to the method Sequence.getDataXY().
	 * </li>
	 * <li>    
	 * Otherwise, the sequence 'target' will be completly cleared (through a
	 * call to the method Sequence.removeAllImages()) when first calling the
	 * method SequenceBuilder.beginUpdate().
	 * </li> 
	 */
	public SequenceBuilder(int sizeX, int sizeY, int sizeZ, int sizeT, int sizeC, DataType dataType, Sequence target)
	{
		_sizeX     = sizeX   ;
		_sizeY     = sizeY   ;
		_sizeZ     = sizeZ   ;
		_sizeT     = sizeT   ;
		_sizeC     = sizeC   ;
		_dataType  = dataType;
		_result    = target==null ? new Sequence() : target;
		_allocator = null;
	}
	
	/**
	 * Size X of the sequence to be created
	 */
	public int getSizeX()
	{
		return _sizeX;
	}

	/**
	 * Size Y of the sequence to be created
	 */
	public int getSizeY()
	{
		return _sizeY;
	}

	/**
	 * Size Z of the sequence to be created
	 */
	public int getSizeZ()
	{
		return _sizeZ;
	}

	/**
	 * Size T of the sequence to be created
	 */
	public int getSizeT()
	{
		return _sizeT;
	}

	/**
	 * Size C of the sequence to be created
	 */
	public int getSizeC()
	{
		return _sizeC;
	}
	
	/**
	 * Data-type of the sequence to be created
	 */
	public DataType getDataType()
	{
		return _dataType;
	}
	
	/**
	 * Return the output sequence
	 */
	public Sequence getResult()
	{
		return _result;
	}
	
	/**
	 * Check if the sequence is pre-allocated, i.e. if it already has the proper
	 * size and data-type that was specified by the argument passed to the
	 * SequenceBuilder constructor 
	 */
	public boolean isPreAllocated()
	{
		return
			
			// Match the size ...
			_result.getSizeX()==_sizeX &&
			_result.getSizeY()==_sizeY &&
			_result.getSizeZ()==_sizeZ &&
			_result.getSizeT()==_sizeT &&
			_result.getSizeC()==_sizeC &&
			
			// ... and the data-type in the case of non-empty sequences
			(_result.getDataType_()==_dataType
				|| _sizeX==0
				|| _sizeY==0
				|| _sizeZ==0
				|| _sizeT==0
				|| _sizeC==0
			);
	}
	
	/**
	 * Start building the sequence
	 * 
	 * @throws IllegalStateException if the object is already in an "updating" state
	 */
	public void beginUpdate()
	{
		if(_allocator!=null) {
			throw new IllegalStateException("The SequenceBuilder object is already in an update state.");
		}
		_result.beginUpdate();
		
		// If the sequence has already the requested size and data-type, it can simply
		// be modified in-place: there is no need for new buffer allocation.
		if(isPreAllocated()) {
			_allocator = new PreAllocatedAllocator(_result);
		}
		
		// Otherwise, the sequence is cleared, and dynamically rebuilt.
		else {
			_result.removeAllImages();
			_allocator = new OnFlyAllocator(_sizeX, _sizeY, _sizeZ, _sizeT, _sizeC, _dataType, _result);
		}
	}
	
	/**
	 * Finish building the sequence.<br>
	 * Nothing happens if beginUpdate() has not been called previously
	 */
	public void endUpdate()
	{
		if(_allocator==null) {
			return;
		}
		_result.endUpdate();
		_allocator = null;
	}
	
	/**
	 * Retrieve or allocate the buffer for the XY plane corresponding to the given (t,z,c) coordinates
	 * @throws NullPointerException if the method beginUpdate() has not been called previously.
	 */
	public double[] getDataAsDouble(int t, int z, int c)
	{
		return (double[])_allocator.getData(t, z, c);
	}
	
	/**
	 * Retrieve or allocate the buffer for the XY plane corresponding to the given (t,z,c) coordinates
	 * @throws NullPointerException if the method beginUpdate() has not been called previously.
	 */
	public float[] getDataAsFloat(int t, int z, int c)
	{
		return (float[])_allocator.getData(t, z, c);
	}
	
	/**
	 * Retrieve or allocate the buffer for the XY plane corresponding to the given (t,z,c) coordinates
	 * @throws NullPointerException if the method beginUpdate() has not been called previously.
	 */
	public byte[] getDataAsByte(int t, int z, int c)
	{
		return (byte[])_allocator.getData(t, z, c);
	}
	
	/**
	 * Retrieve or allocate the buffer for the XY plane corresponding to the given (t,z,c) coordinates
	 * @throws NullPointerException if the method beginUpdate() has not been called previously.
	 */
	public short[] getDataAsShort(int t, int z, int c)
	{
		return (short[])_allocator.getData(t, z, c);
	}
	
	/**
	 * Retrieve or allocate the buffer for the XY plane corresponding to the given (t,z,c) coordinates
	 * @throws NullPointerException if the method beginUpdate() has not been called previously.
	 */
	public int[] getDataAsInt(int t, int z, int c)
	{
		return (int[])_allocator.getData(t, z, c);
	}
	
	/**
	 * Retrieve or allocate the buffer for the XY plane corresponding to the given (t,z,c) coordinates
	 * @throws NullPointerException if the method beginUpdate() has not been called previously.
	 */
	public Object getData(int t, int z, int c)
	{
		return _allocator.getData(t, z, c);
	}
	
	/**
	 * Validate the buffer for the XY plane corresponding to the given (t,z,c) coordinates
	 * @throws NullPointerException if the method beginUpdate() has not been called previously.
	 */
	public void validateData(int t, int z, int c)
	{
		_allocator.validateData(t, z, c);
	}

	
	/**
	 * Interface to access the data of the targeted sequence
	 */
	private interface SequenceAllocator
	{
		/**
		 * Retrieve or allocate the buffer for the XY plane corresponding to the given (t,z,c) coordinates
		 */
		public Object getData(int t, int z, int c);
		
		/**
		 * Validate the buffer for the XY plane corresponding to the given (t,z,c) coordinates
		 */
		public void validateData(int t, int z, int c);
	}
	
	
	/**
	 * Virtual allocator dedicated to pre-allocated sequences
	 */
	private static class PreAllocatedAllocator implements SequenceAllocator
	{
		private Sequence _target;
		
		public PreAllocatedAllocator(Sequence target)
		{
			_target = target;
		}
		
		@Override
		public Object getData(int t, int z, int c)
		{
			return _target.getDataXY(t, z, c);
		}

		@Override
		public void validateData(int t, int z, int c)
		{
			// Nothing to do
		}
	}
	
	
	/**
	 * Allocator use for dynamically allocated sequences
	 */
	private static class OnFlyAllocator implements SequenceAllocator
	{
		private int              _sizeZ;
		private ImageAllocator[] _image;
		
		public OnFlyAllocator(int sizeX, int sizeY, int sizeZ, int sizeT, int sizeC, DataType dataType, Sequence target)
		{
			int offset = 0;
			int sizeZT = sizeT*sizeZ;
			_sizeZ = sizeZ;
			_image = new ImageAllocator[sizeZT];
			for(int t=0; t<sizeT; ++t) {
				for(int z=0; z<sizeZ; ++z) {
					_image[offset] = new ImageAllocator(sizeX, sizeY, sizeC, dataType, target, t, z);
					++offset;
				}
			}
		}

		@Override
		public Object getData(int t, int z, int c)
		{
			return _image[z+_sizeZ*t].getData(c);
		}

		@Override
		public void validateData(int t, int z, int c)
		{
			_image[z+_sizeZ*t].validateData(c);
		}
	}
	
	
	/**
	 * Provide tools to build the several IcyBufferedImage objects that compose
	 * the final sequence
	 */
	private static class ImageAllocator
	{
		private int       _sizeX    ;
		private int       _sizeY    ;
		private DataType  _dataType ;
		private Sequence  _target   ;
		private int       _t        ;
		private int       _z        ;
		private boolean   _done     ;
		private boolean[] _available;
		private Object[]  _data     ;
		
		/**
		 * Constructor
		 */
		public ImageAllocator(int sizeX, int sizeY, int sizeC, DataType dataType, Sequence target, int t, int z)
		{
			_sizeX     = sizeX   ;
			_sizeY     = sizeY   ;
			_dataType  = dataType;
			_target    = target  ;
			_t         = t       ;
			_z         = z       ;
			_done      = false;
			_data      = Array2DUtil.createArray(_dataType, sizeC);
			_available = new boolean[sizeC];
			for(int c=0; c<sizeC; ++c) {
				_available[c] = false;
			}
		}
		
		/**
		 * Allocate the buffer corresponding to the given channel
		 */
		public Object getData(int c)
		{
			if(_data[c]==null) {
				_data[c] = Array1DUtil.createArray(_dataType, _sizeX*_sizeY);
			}
			return _data[c];
		}
		
		/**
		 * Validate the buffer corresponding to the given channel.<br>
		 * If the results for all the channels are available, try to create the
		 * final buffered image
		 */
		public void validateData(int c)
		{
			// Mark the current channel as valid and available
			_available[c] = true;
			
			// Check whether the results for all the channels are available
			for(boolean b : _available) {
				if(!b) {
					return;
				}
			}
			
			// Synchronization is needed here, as several threads may try to create the
			// final image at the same time
			synchronized (this)
			{
				// Nothing to do if the image has been finalized during the synchronization
				if(_done) {
					return;
				}
				
				// Create the buffered image, and update the sequence
				IcyBufferedImage image = new IcyBufferedImage(_sizeX, _sizeY, _data);
				synchronized (_target) {
					_target.setImage(_t, _z, image);
				}
				
				// Flag the image allocator as finalized
				_done = true;
			}
		}
	}
}
