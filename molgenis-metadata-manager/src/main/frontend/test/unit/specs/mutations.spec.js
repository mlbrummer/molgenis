import mutations from 'store/mutations'

describe('mutations', () => {
  describe('Testing mutation SET_PACKAGES', () => {
    it('Sets package', () => {
      const state = {
        packages: []
      }
      const packages = [{ id: 'sys_idx', label: 'Index' },
        { id: 'sys_sec', label: 'Security' }, { id: 'sys', label: 'System' },
        { id: 'sys_md', label: 'Meta' }, { id: 'base', label: 'Default' }]
      mutations.__SET_PACKAGES__(state, packages)
      expect(state.packages).to.equal(packages)
    })
  })

  describe('Testing mutation CREATE_ALERT', () => {
    it('Updates alert message', () => {
      const state = {
        alert: {
          message: null,
          type: null
        }
      }
      const alert = {
        message: 'Hello',
        type: 'success'
      }
      mutations.__CREATE_ALERT__(state, alert)
      expect(state.alert.message).to.equal('Hello')
    })
    it('Updates alert type', () => {
      const state = {
        alert: {
          message: null,
          type: null
        }
      }
      const alert = {
        message: 'Hello',
        type: 'success'
      }
      mutations.__CREATE_ALERT__(state, alert)
      expect(state.alert.type).to.equal('success')
    })
  })

  describe('Testing mutation SET_ENTITY_TYPES', () => {
    it('Sets entity types', () => {
      const state = {
        alert: {
          entityTypes: []
        }
      }

      const payload = [
        { label: 'B entity' },
        { label: 'A entity' },
        { label: 'C entity' },
        { label: 'E entity' },
        { label: 'D entity' }
      ]

      const expected = [
        { label: 'A entity' },
        { label: 'B entity' },
        { label: 'C entity' },
        { label: 'D entity' },
        { label: 'E entity' }
      ]

      mutations.__SET_ENTITY_TYPES__(state, payload)
      expect(state.entityTypes).to.deep.equal(expected)
    })
  })

  describe('Testing mutation SET_ATTRIBUTE_TYPES', () => {
    it('should set a list of attribute types', () => {
      const state = {
        attributeTypes: []
      }

      const attributeTypes = ['STRING', 'INT', 'XREF']

      mutations.__SET_ATTRIBUTE_TYPES__(state, attributeTypes)
      expect(state.attributeTypes).to.deep.equal(attributeTypes)
    })
  })

  describe('Testing mutation SET_EDITOR_ENTITY_TYPE', () => {
    it('Sets selected entity type to edit', () => {
      const state = {
        editorEntityType: {},
        initialEditorEntityType: {}
      }
      const editorEntityType = {
        id: 'root_gender',
        labelI18n: {},
        description: 'Gender options',
        abstract0: false,
        attributes: [
          {
            aggregatable: false,
            auto: false,
            descriptionI18n: {},
            enumOptions: [],
            id: 'bla',
            labelI18n: {},
            name: 'id',
            nullable: false,
            readonly: true,
            tags: [],
            type: 'STRING',
            unique: true,
            visible: true
          },
          {
            aggregatable: false,
            auto: false,
            descriptionI18n: {},
            enumOptions: [],
            id: 'bladibla',
            labelI18n: {},
            name: 'label',
            nullable: false,
            readonly: true,
            tags: [],
            type: 'STRING',
            unique: true,
            visible: true
          }
        ],
        backend: 'postgreSQL',
        idAttribute: { id: 'bla', label: 'id' },
        label: 'Gender',
        labelAttribute: { id: 'bladibla', label: 'label' },
        lookupAttributes: [
          { id: 'bla', label: 'id' },
          { id: 'bladibla', label: 'label' }
        ],
        package0: { id: 'root', label: 'root' },
        tags: []
      }
      mutations.__SET_EDITOR_ENTITY_TYPE__(state, editorEntityType)
      expect(state.editorEntityType).to.deep.equal(editorEntityType)
      expect(state.initialEditorEntityType).to.deep.equal(JSON.parse(JSON.stringify(editorEntityType)))
    })
  })

  describe('Testing mutation UPDATE_EDITOR_ENTITY_TYPE', () => {
    it('should update the description of the EditorEntityType', () => {
      const state = {
        editorEntityType: {
          description: 'description'
        }
      }

      const update = {
        key: 'description',
        value: 'Option-selection-list for gender'
      }

      mutations.__UPDATE_EDITOR_ENTITY_TYPE__(state, update)
      expect(state.editorEntityType.description).to.equal('Option-selection-list for gender')
    })

    it('should update the idAttribute of the EditorEntityType', () => {
      const state = {
        editorEntityType: {
          idAttribute: null,
          attributes: [
            { id: '1' }
          ]
        }
      }

      const update = {
        key: 'idAttribute',
        value: { id: '1', label: 'idAttribute' }
      }

      const expected = {
        editorEntityType: {
          idAttribute: { id: '1', label: 'idAttribute', readonly: true, unique: true, nullable: false },
          attributes: [
            { id: '1', label: 'idAttribute', readonly: true, unique: true, nullable: false }
          ]
        }
      }

      mutations.__UPDATE_EDITOR_ENTITY_TYPE__(state, update)
      expect(state).to.deep.equal(expected)
    })
  })

  describe('Testing mutation UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE', () => {
    it('Updates the selected attribute IN the editorEntityType attribute list', () => {
      const state = {
        selectedAttributeId: '2',
        editorEntityType: {
          attributes: [
            { id: '1', name: 'attribute1' },
            { id: '2', name: 'attribute2' },
            { id: '3', name: 'attribute3' }
          ]
        }
      }

      const expected = [
        { id: '1', name: 'attribute1' },
        { id: '2', name: 'updated name' },
        { id: '3', name: 'attribute3' }
      ]

      mutations.__UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE__(state, { key: 'name', value: 'updated name' })
      expect(state.editorEntityType.attributes).to.deep.equal(expected)
    })
  })

  describe('Testing mutation SET_SELECTED_ATTRIBUTE_ID', () => {
    it('Updates the selected attribute ID', () => {
      const state = {
        selectedAttributeId: null
      }

      const id = 'newAttributeId'
      mutations.__SET_SELECTED_ATTRIBUTE_ID__(state, id)
      expect(state.selectedAttributeId).to.equal('newAttributeId')
    })
  })

  describe('Testing mutation DELETE_SELECTED_ATTRIBUTE', () => {
    it('should remove an attribute from the list of editorEntityType attributes based on the selected attribute id', () => {
      const state = {
        selectedAttributeId: '1',
        editorEntityType: {
          attributes: [
            { id: '1', name: 'attribute1' },
            { id: '2', name: 'attribute2' },
            { id: '3', name: 'attribute3' }
          ]
        }
      }

      const expected = [
        { id: '2', name: 'attribute2' },
        { id: '3', name: 'attribute3' }
      ]

      mutations.__DELETE_SELECTED_ATTRIBUTE__(state, '1')
      expect(state.editorEntityType.attributes).to.deep.equal(expected)
    })
  })

  describe('Testing mutation UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE_ORDER', () => {
    it('should move the index of the selected attribute from 2 to 1', () => {
      const state = {
        editorEntityType: {
          attributes: [
            { id: '1', name: 'attribute1' },
            { id: '2', name: 'attribute2' },
            { id: '3', name: 'attribute3' }
          ]
        }
      }

      const expected = [
        { id: '1', name: 'attribute1' },
        { id: '3', name: 'attribute3' },
        { id: '2', name: 'attribute2' }
      ]

      mutations.__UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE_ORDER__(state, { moveOrder: 'up', selectedAttributeIndex: 2 })
      expect(state.editorEntityType.attributes).to.deep.equal(expected)
    })

    it('should move the index of the selected attribute from 0 to 1', () => {
      const state = {
        editorEntityType: {
          attributes: [
            { id: '1', name: 'attribute1' },
            { id: '2', name: 'attribute2' },
            { id: '3', name: 'attribute3' }
          ]
        }
      }

      const expected = [
        { id: '2', name: 'attribute2' },
        { id: '1', name: 'attribute1' },
        { id: '3', name: 'attribute3' }
      ]

      mutations.__UPDATE_EDITOR_ENTITY_TYPE_ATTRIBUTE_ORDER__(state, { moveOrder: 'down', selectedAttributeIndex: 0 })
      expect(state.editorEntityType.attributes).to.deep.equal(expected)
    })
  })
})
