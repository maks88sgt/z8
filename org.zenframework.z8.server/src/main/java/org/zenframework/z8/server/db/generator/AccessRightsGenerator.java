package org.zenframework.z8.server.db.generator;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.zenframework.z8.server.base.query.RecordLock;
import org.zenframework.z8.server.base.table.Table;
import org.zenframework.z8.server.base.table.system.Fields;
import org.zenframework.z8.server.base.table.system.Requests;
import org.zenframework.z8.server.base.table.system.RoleFieldAccess;
import org.zenframework.z8.server.base.table.system.RoleRequestAccess;
import org.zenframework.z8.server.base.table.system.RoleTableAccess;
import org.zenframework.z8.server.base.table.system.Roles;
import org.zenframework.z8.server.base.table.system.Tables;
import org.zenframework.z8.server.base.table.value.Field;
import org.zenframework.z8.server.db.Connection;
import org.zenframework.z8.server.db.ConnectionManager;
import org.zenframework.z8.server.db.FieldType;
import org.zenframework.z8.server.db.sql.expressions.And;
import org.zenframework.z8.server.db.sql.expressions.Equ;
import org.zenframework.z8.server.engine.Runtime;
import org.zenframework.z8.server.runtime.OBJECT;
import org.zenframework.z8.server.security.IAccess;
import org.zenframework.z8.server.security.IRole;
import org.zenframework.z8.server.types.bool;
import org.zenframework.z8.server.types.guid;
import org.zenframework.z8.server.types.integer;
import org.zenframework.z8.server.types.string;

public class AccessRightsGenerator {
	private Tables tables = new Tables.CLASS<Tables>().get();
	private Fields fields = new Fields.CLASS<Fields>().get();
	private Requests requests = new Requests.CLASS<Requests>().get();

	private RoleTableAccess rta = new RoleTableAccess.CLASS<RoleTableAccess>().get();
	private RoleFieldAccess rfa = new RoleFieldAccess.CLASS<RoleFieldAccess>().get();
	private RoleRequestAccess rra = new RoleRequestAccess.CLASS<RoleRequestAccess>().get();

	private Collection<guid> tableKeys = new HashSet<guid>();
	private Collection<guid> requestKeys = new HashSet<guid>();
	private Collection<IRole> roles = null;

	@SuppressWarnings("unused")
	private ILogger logger;

	public AccessRightsGenerator(ILogger logger) {
		this.logger = logger;
		tableKeys.addAll(Runtime.instance().tableKeys());
		requestKeys.addAll(Runtime.instance().requestKeys());
	}

	public void run() {
		Connection connection = ConnectionManager.get();

		try {
			connection.beginTransaction();

			clearTables();
			createTables();

			clearRequests();
			createRequests();

			connection.commit();
		} catch(Throwable e) {
			connection.rollback();
			throw new RuntimeException(e);
		} finally {
			connection.release();
		}
	}

	private void clearTables() {
		tables.read(Arrays.asList(tables.primaryKey()), tables.primaryKey().notInVector(tableKeys));

		while(tables.next()) {
			guid tableId = tables.recordId();
			rta.destroy(new Equ(rta.table.get(), tableId));
			rfa.destroy(new Equ(rfa.fields.get().table.get(), tableId));
			fields.destroy(new Equ(fields.table.get(), tableId));
			tables.destroy(tableId);
		}
	}

	private void createTables() {
		tables.read(Arrays.asList(tables.primaryKey()), tables.primaryKey().inVector(tableKeys));
		while(tables.next()) {
			guid tableId = tables.recordId();
			Table table = Runtime.instance().getTableByKey(tableId).newInstance();
			setTableProperties(table);
			tables.update(tableId);

			updateFields(table);
			tableKeys.remove(tableId);
		}

		for(guid key : tableKeys) {
			Table table = Runtime.instance().getTableByKey(key).newInstance();
			setTableProperties(table);
			tables.create(key);

			createFields(table);

			for(IRole role : getRoles()) {
				IAccess access = role.access();

				rta.role.get().set(role.id());
				rta.table.get().set(key);

				rta.read.get().set(new bool(access.read()));
				rta.write.get().set(new bool(access.write()));
				rta.create.get().set(new bool(access.create()));
				rta.copy.get().set(new bool(access.copy()));
				rta.destroy.get().set(new bool(access.destroy()));
				rta.create();
			}
		}
	}

	private void clearRequests() {
		requests.read(Arrays.asList(requests.primaryKey()), requests.primaryKey().notInVector(requestKeys));

		while(requests.next()) {
			guid requestId = requests.recordId();
			rra.destroy(new Equ(rra.request.get(), requestId));
			requests.destroy(requestId);
		}
	}

	private void createRequests() {
		requests.read(Arrays.asList(requests.primaryKey()), requests.primaryKey().inVector(requestKeys));
		while(requests.next()) {
			guid requestId = requests.recordId();
			OBJECT request = Runtime.instance().getRequestByKey(requestId).newInstance();
			setRequestProperties(request);
			requests.update(requestId);
			requestKeys.remove(requestId);
		}

		for(guid key : requestKeys) {
			OBJECT request = Runtime.instance().getRequestByKey(key).newInstance();
			setRequestProperties(request);
			requests.create(key);

			for(IRole role : getRoles()) {
				IAccess access = role.access();

				rra.role.get().set(role.id());
				rra.request.get().set(key);

				rra.execute.get().set(new bool(access.execute()));
				rra.create();
			}
		}
	}

	private void updateFields(Table table) {
		Map<guid, Field> fieldsMap = table.getFieldsMap();
		Collection<guid> fieldKeys = new HashSet<guid>(fieldsMap.keySet());

		fields.read(Arrays.asList(fields.primaryKey()),
				new And(new Equ(fields.table.get(), table.key()), fields.primaryKey().notInVector(fieldKeys)));

		while(fields.next()) {
			guid fieldId = fields.recordId();
			rfa.destroy(new Equ(rfa.field.get(), fieldId));
			fields.destroy(fieldId);
		}

		createFields(table);
	}

	private void createFields(Table table) {
		Map<guid, Field> fieldsMap = table.getFieldsMap();
		Collection<guid> fieldKeys = new HashSet<guid>(fieldsMap.keySet());

		fields.read(Arrays.asList(fields.primaryKey()), fields.primaryKey().inVector(fieldKeys));
		while(fields.next()) {
			guid fieldId = fields.recordId();
			setFieldProperties(fieldsMap.get(fieldId), table.key());
			fields.update(fieldId);

			fieldKeys.remove(fieldId);
		}

		for(guid key : fieldKeys) {
			setFieldProperties(fieldsMap.get(key), table.key());
			fields.create(key);
			for(IRole role : getRoles()) {
				IAccess access = role.access();

				rfa.role.get().set(role.id());
				rfa.field.get().set(key);

				rfa.read.get().set(new bool(access.read()));
				rfa.write.get().set(new bool(access.write()));
				rfa.create();
			}
		}
	}

	private void setTableProperties(Table table) {
		tables.classId.get().set(new string(table.classId()));
		tables.name.get().set(new string(table.name()));
		tables.displayName.get().set(new string(table.displayName()));
		tables.description.get().set(new string(table.description()));
		tables.lock.get().set(RecordLock.Full);
	}

	private void setFieldProperties(Field field, guid tableKey) {
		fields.table.get().set(tableKey);
		fields.name.get().set(new string(field.name()));
		fields.displayName.get().set(new string(displayName(field)));
		fields.description.get().set(new string(field.description()));
		fields.type.get().set(new string(getFieldType(field)));
		fields.position.get().set(new integer(field.ordinal()));
		fields.lock.get().set(RecordLock.Full);
	}

	private void setRequestProperties(OBJECT request) {
		requests.classId.get().set(request.classId());
		requests.name.get().set(new string(request.displayName()));
		requests.lock.get().set(RecordLock.Full);
	}

	private Collection<IRole> getRoles() {
		if(roles == null) {
			Roles rolesTable = new Roles.CLASS<Roles>(null).get();
			roles = rolesTable.get();
		}
		return roles;
	}

	private String displayName(Field field) {
		String name = field.displayName();

		if(name != null && !name.isEmpty())
			return name;

		name = field.name();
		return name == null || name.isEmpty() ? field.getClass().getSimpleName() : name;
	}

	private String getFieldType(Field field) {
		FieldType type = field.type();
		return type.toString() + (type == FieldType.String ? "(" + field.length.get() + ")" : "");
	}
}
