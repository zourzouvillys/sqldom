package io.zrz.sqldom.types;

public interface SimpleTypeUse extends TypeUse {

  /**
   * The type iself.
   */

  SqlType type();

  /**
   * if the type is an array, how many dimentions there are to it.
   *
   * e.g, int[] == 1, int[][] == 2, etc.
   *
   * @return
   */

  int arraydim();

}
