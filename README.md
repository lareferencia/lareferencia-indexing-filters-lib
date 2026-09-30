# LA Referencia Indexing Filters Library

Field occurrence filtering library for controlling entity metadata indexing.

## 🎯 Functionality

Provides configurable `FieldOccurrenceFilter` strategies that limit which
occurrences of a metadata field are written to the index, reducing index size
and improving search relevance. The installed strategies are:

| Filter | Selection criterion |
|---|---|
| `LongestStringFieldOccurrenceFilter` | Longest string value(s) of the field |
| `OldestDateFieldOccurrenceFilter` | Oldest date value(s) of the field |
| `RegexFieldOccurrenceFilter` | Values matching a configured regular expression |
| `SubfieldsFormatFieldOccurrenceFilter` | Values whose subfields match a format |

Filters are wired into the entity indexing configuration (see
[`docs/ENTITY_INDEXING_ARCHITECTURE.md`](../docs/ENTITY_INDEXING_ARCHITECTURE.md)).

## 📄 License

Licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**.  
See [LICENSE.txt](../LICENSE.txt) for complete terms.

## 📧 Support

**Email**: soporte@lareferencia.redclara.net

---

**LA Referencia** - Red Latinoamericana y de España de Ciencia Abierta  
Part of the LA Referencia Platform 5.0.0-rc2
