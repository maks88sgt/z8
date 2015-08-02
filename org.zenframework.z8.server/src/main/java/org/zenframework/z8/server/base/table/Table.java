package org.zenframework.z8.server.base.table;

import java.util.Collection;

import org.zenframework.z8.server.base.query.Query;
import org.zenframework.z8.server.base.table.value.BoolExpression;
import org.zenframework.z8.server.base.table.value.BoolField;
import org.zenframework.z8.server.base.table.value.DatetimeField;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.base.table.value.GuidField;
import org.zenframework.z8.server.base.table.value.StringField;
import org.zenframework.z8.server.base.table.value.TextField;
import org.zenframework.z8.server.db.sql.SqlToken;
import org.zenframework.z8.server.resources.Resources;
import org.zenframework.z8.server.runtime.IObject;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.datetime;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;

public class Table extends TableBase {
    static public class names {
        public final static String RecordId = "RecordId";
        public final static String CreatedAt = "CreatedAt";
        public final static String CreatedBy = "CreatedBy";
        public final static String ModifiedAt = "ModifiedAt";
        public final static String ModifiedBy = "ModifiedBy";
        public final static String Scope = "Scope";

        public final static String Id = "Id";
        public final static String Id1 = "Id1";
        public final static String Name = "Name";
        public final static String Description = "Description";
        public final static String Locked = "Locked";
        public final static String LockedExpression = "LockedExpression";
    }

    static public class strings {
        public final static String RecordId = "Table.recordId";
        public final static String CreatedAt = "Table.createdAt";
        public final static String CreatedBy = "Table.createdBy";
        public final static String ModifiedAt = "Table.modifiedAt";
        public final static String ModifiedBy = "Table.modifiedBy";
        public final static String Scope = "Table.scope";

        public final static String Id = "Table.id";
        public final static String Id1 = "Table.id1";
        public final static String Name = "Table.name";
        public final static String Description = "Table.description";
        public final static String Locked = "Table.locked";
        public final static String IsDefault = "Table.isDefault";
    }

    public static class CLASS<T extends Table> extends TableBase.CLASS<T> {
        public CLASS(IObject container) {
            super(container);
            setJavaClass(Table.class);
            setAttribute(Native, Table.class.getCanonicalName());
        }

        @Override
        public Object newObject(IObject container) {
            return new Table(container);
        }
    }

    public GuidField.CLASS<GuidField> recordId = new GuidField.CLASS<GuidField>(this);

    public DatetimeField.CLASS<? extends DatetimeField> createdAt = new DatetimeField.CLASS<DatetimeField>(this);
    public DatetimeField.CLASS<? extends DatetimeField> modifiedAt = new DatetimeField.CLASS<DatetimeField>(this);

    public GuidField.CLASS<? extends GuidField> createdBy = new GuidField.CLASS<GuidField>(this);
    public GuidField.CLASS<? extends GuidField> modifiedBy = new GuidField.CLASS<GuidField>(this);

    public StringField.CLASS<? extends StringField> id = new StringField.CLASS<StringField>(this);
    public StringField.CLASS<? extends StringField> id1 = new StringField.CLASS<StringField>(this);
    public StringField.CLASS<? extends StringField> name = new StringField.CLASS<StringField>(this);
    public TextField.CLASS<? extends StringField> description = new TextField.CLASS<TextField>(this);

    private BoolField.CLASS<? extends BoolField> lockedField = new BoolField.CLASS<BoolField>(this);

    public BoolExpression.CLASS<? extends BoolExpression> locked = new lockedExpression.CLASS<lockedExpression>(this);

    public static class lockedExpression extends BoolExpression {
        public static class CLASS<T extends lockedExpression> extends BoolExpression.CLASS<T> {
            public CLASS(IObject container) {
                super(container);
                setJavaClass(lockedExpression.class);
            }

            @Override
            public Object newObject(IObject container) {
                return new lockedExpression(container);
            }
        }

        public lockedExpression(IObject container) {
            super(container);
        }

        @Override
        public SqlToken z8_expression() {
            Table table = (Table)getContainer();
            return table.lockedField.get().sql_bool();
        }
    }

    final static public int IdLength = 15;
    final static public int Id1Length = 15;
    final static public int NameLength = 50;
    final static public int NameWidth = 20;

    public Table(IObject container) {
        super(container);
    }

    @Override
    public void constructor2() {
        super.constructor2();

        id.setName(names.Id);
        id.setIndex("id");
        id.setDisplayName(Resources.get(strings.Id));
        id.get().length.set(IdLength);
        id.get().width.set(IdLength);
        id.get().stretch.set(false);

        id1.setName(names.Id1);
        id1.setIndex("id1");
        id1.setDisplayName(Resources.get(strings.Id1));
        id1.get().length = new integer(Id1Length);
        id1.get().width.set(Id1Length);
        id1.get().stretch.set(false);

        name.setName(names.Name);
        name.setIndex("name");
        name.setDisplayName(Resources.get(strings.Name));
        name.get().length = new integer(NameLength);
        name.get().width.set(NameWidth);

        description.setName(names.Description);
        description.setIndex("description");
        description.setDisplayName(Resources.get(strings.Description));

        lockedField.setName(names.Locked);
        lockedField.setIndex("locked");
        lockedField.setDisplayName(Resources.get(strings.Locked));
        lockedField.get().system = new bool(true);

        locked.setIndex("lockedExpression");
        locked.setDisplayName(Resources.get(strings.Locked));
        locked.get().system = new bool(true);
        locked.get().readOnly = new bool(false);

        recordId.setName(names.RecordId);
        recordId.setIndex("recordId");
        recordId.setDisplayName(Resources.get(strings.RecordId));
        recordId.setAttribute(PrimaryKey, "");
        recordId.get().system = new bool(true);

        createdAt.setName(names.CreatedAt);
        createdAt.setIndex("createdAt");
        createdAt.setDisplayName(Resources.get(strings.CreatedAt));
        createdAt.get().system = new bool(true);

        createdBy.setName(names.CreatedBy);
        createdBy.setIndex("createdBy");
        createdBy.setDisplayName(Resources.get(strings.CreatedBy));
        createdBy.get().system = new bool(true);

        modifiedAt.setName(names.ModifiedAt);
        modifiedAt.setIndex("modifiedAt");
        modifiedAt.setDisplayName(Resources.get(strings.ModifiedAt));
        modifiedAt.get().system = new bool(true);

        modifiedBy.setName(names.ModifiedBy);
        modifiedBy.setIndex("modifiedBy");
        modifiedBy.setDisplayName(Resources.get(strings.ModifiedBy));
        modifiedBy.get().system = new bool(true);

        registerDataField(recordId);
        registerDataField(createdAt);
        registerDataField(modifiedAt);
        registerDataField(createdBy);
        registerDataField(modifiedBy);

        registerDataField(id);
        registerDataField(id1);
        registerDataField(name);
        registerDataField(description);
        registerDataField(lockedField);
        registerDataField(locked);
    }

    @Override
    public Field primaryKey() {
        return recordId.get();
    }
    
    @Override
    protected void beforeCreate(Query data, guid recordId, guid parentId, Query model, guid modelRecordId) {
        createdAt.get().set(new datetime());
        createdBy.get().set(getUser().id());

        lockedField.get().set(locked.get().get());

        super.beforeCreate(data, recordId, parentId, model, modelRecordId);
    }

    @Override
    protected void beforeUpdate(Query data, guid recordId, Collection<Field> fields, Query model, guid modelRecordId) {
        if(data == this && !fields.isEmpty()) {
            modifiedAt.get().set(new datetime());
            modifiedBy.get().set(getUser().id());
        }

        if(fields.contains(locked.get())) {
            lockedField.get().set(locked.get().get());
        }

        super.beforeUpdate(data, recordId, fields, model, modelRecordId);
    }

    public void beforeGenerate() {
        z8_beforeGenerate();
    }

    public void afterGenerate() {
        z8_afterGenerate();
    }

    public void z8_beforeGenerate() {}

    public void z8_afterGenerate() {}

}