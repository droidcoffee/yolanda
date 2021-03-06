/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2011, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc..
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.metamodel.source.annotations.xml.mocker;

import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbVersion;

import org.jboss.jandex.ClassInfo;

/**
 * @author Strong Liu
 */
class VersionMocker extends PropertyMocker {
	private JaxbVersion version;

	VersionMocker(IndexBuilder indexBuilder, ClassInfo classInfo, EntityMappingsMocker.Default defaults, JaxbVersion version) {
		super( indexBuilder, classInfo, defaults );
		this.version = version;
	}

	@Override
	protected String getFieldName() {
		return version.getName();
	}

	@Override
	protected void processExtra() {
		create( VERSION );
		parserColumn( version.getColumn(), getTarget() );
		parserTemporalType( version.getTemporal(), getTarget() );
	}

	@Override
	protected JaxbAccessType getAccessType() {
		return version.getAccess();
	}

	@Override
	protected void setAccessType(JaxbAccessType accessType) {
		version.setAccess( accessType );
	}
}
