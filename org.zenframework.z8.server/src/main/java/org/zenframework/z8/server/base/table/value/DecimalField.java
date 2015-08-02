package org.zenframework.z8.server.base.table.value;

import org.zenframework.z8.server.db.DatabaseVendor;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.SqlField;
import org.zenframework.z8.server.format.Format;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.decimal;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.primary;
import org.zenframework.z8.server.types.sql.sql_decimal;
import org.zenframework.z8.server.types.sql.sql_primary;

public class DecimalField extends Field {
    public static class CLASS<T extends DecimalField> extends Field.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(DecimalField.class);
            setAttribute("native", DecimalField.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new DecimalField(container);
        }
    }

    public integer precision = new integer(19);
    public integer scale = new integer(4);

    public DecimalField(IObject container) {
        super(container);
        setDefault(new decimal());
        format.set(Format.decimal);
        stretch.set(false);
    }

    public decimal z8_getDefault() {
        return (decimal)super.getDefault();
    }

    @Override
    public primary getDefault() {
        return z8_getDefault();
    }

    @Override
    public FieldType type() {
        return FieldType.Decimal;
    }

    @Override
    public int size() {
        return precision.getInt();
    }

    @Override
    public int scale() {
        return scale.getInt();
    }

    @Override
    public String sqlType(DatabaseVendor vendor) {
        String name = type().vendorType(vendor);
        return name + "(" + precision.get() + ", " + scale + ")";
    }

    public sql_decimal sql_decimal() {
        return new sql_decimal(new SqlField(this));
    }

    @Override
    public primary get() {
        return z8_get();
    }

    public decimal z8_get() {
        return (decimal)internalGet();
    }

    @Override
    public sql_primary formula() {
        return z8_formula();
    }

    public sql_decimal z8_formula() {
        return null;
    }

    public DecimalField.CLASS<? extends DecimalField> operatorAssign(decimal value) {
        set(value);
        return (DecimalField.CLASS<?>) this.getCLASS();
    }
}