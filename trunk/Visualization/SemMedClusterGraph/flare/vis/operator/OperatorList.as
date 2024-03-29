package flare.vis.operator
{
	import flare.animate.Transitioner;
	import flare.util.Arrays;
	import flare.vis.Visualization;

	import flash.utils.Proxy;
	import flash.utils.flash_proxy;
	import mx.controls.Alert;

	/**
	 * An OperatorList maintains a sequential chain of operators that are
	 * invoked one after the other. Operators can be added to an OperatorList
	 * using the <code>add</code> method. Once added, operators can be
	 * retrieved and set using their index in the lists, either with array
	 * notation (<code>[]</code>) or with the <code>getOperatorAt</code> and
	 * <code>setOperatorAt</code> methods.
	 */
	public class OperatorList extends Proxy implements IOperator
	{
		// -- Properties ------------------------------------------------------

		protected var _vis:Visualization;
		protected var _list:Array = new Array();

		/** The visualization processed by this operator. */
		public function get visualization():Visualization { return _vis; }
		public function set visualization(v:Visualization):void
		{
			_vis = v; setup();
			for each (var op:IOperator in _list) {
				op.visualization = v;
			}
		}

		/** The number of operators in the list. */
		public function get length():uint { return _list.length; }

		// --------------------------------------------------------------------

		/**
		 * Creates a new OperatorList.
		 * @param ops an ordered set of operators to include in the list.
		 */
		public function OperatorList(...ops) {
			for each (var op:IOperator in ops) {
				add(op);
			}
		}

		/** @inheritDoc */
		public function setup():void
		{
			// do nothing
		}

		/**
		 * Proxy method for retrieving operators from the internal array.
		 */
		flash_proxy override function getProperty(name:*):*
		{
    		return _list[name];
    	}

		/**
		 * Proxy method for setting operators in the internal array.
		 */
    	flash_proxy override function setProperty(name:*, value:*):void
    	{
        	if (value is IOperator) {
        		var op:IOperator = IOperator(value);
        		_list[name] = op;
        		op.visualization = this.visualization;
        	} else {
        		throw new ArgumentError("Input value must be an IOperator.");
        	}
    	}

		/**
		 * Returns the operator at the specified position in the list
		 * @param i the index into the operator list
		 * @return the requested operator
		 */
		public function getOperatorAt(i:uint):IOperator
		{
			return _list[i];
		}

		/**
		 * Removes the operator at the specified position in the list
		 * @param i the index into the operator list
		 * @return the removed operator
		 */
		public function removeOperatorAt(i:uint):IOperator
		{
			return Arrays.removeAt(_list, i) as IOperator;
		}

		/**
		 * Set the operator at the specified position in the list
		 * @param i the index into the operator list
		 * @param op the operator to place in the list
		 * @return the operator previously at the index
		 */
		public function setOperatorAt(i:uint, op:IOperator):IOperator
		{
			var old:IOperator = _list[i];
			op.visualization = visualization;
			_list[i] = op;
			return old;
		}

		/**
		 * Adds an operator to the end of this list.
		 * @param op the operator to add
		 */
		public function add(op:IOperator):void
		{
			op.visualization = visualization;
			_list.push(op);
		}

		/**
		 * Removes an operator from this list.
		 * @param op the operator to remove
		 * @return true if the operator was found and removed, false otherwise
		 */
		public function remove(op:IOperator):Boolean
		{
			return Arrays.remove(_list, op) >= 0;
		}

		/**
		 * Removes all operators from this list.
		 */
		public function clear():void
		{
			Arrays.clear(_list);
		}

		/** @inheritDoc */
		public function operate(t:Transitioner=null):void
		{
			// Alert.show('step 1 in operate()');
			t = (t!=null ? t : Transitioner.DEFAULT);
			// Alert.show('step 2 in operate()');
			for each (var op:IOperator in _list) {
				// Alert.show('step 3 in operate()');
				op.operate(t);
			}
		}

	} // end of class OperatorList
}