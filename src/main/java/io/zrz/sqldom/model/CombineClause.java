package io.zrz.sqldom.model;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, from = "toImmutable")
@Value.Modifiable
public interface CombineClause {

  SelectStatement source();

  enum CombineType {

    Union,

    Intersect,

    Except

  }

  CombineType type();

  enum CombineMode {

    All,

    Distinct

  }

  CombineMode mode();

}
