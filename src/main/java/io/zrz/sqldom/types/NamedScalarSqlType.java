package io.zrz.sqldom.types;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PUBLIC, depluralize = true)
@Value.Modifiable
public interface NamedScalarSqlType extends ScalarSqlType {

  String typename();

}
