function CustomDelegate() {}

CustomDelegate.prototype.setRequestContext = function(context) {
  this.ctx = context;
};

CustomDelegate.prototype.authorize = function()  {
  switch(this.ctx.identifier) {
    case 'forbidden.jpg':
    case 'forbidden-boolean.jpg':
      return false;
    case 'forbidden-code.jpg':
      return {
        'status_code': 401,
        'challenge': 'Basic',
      };
    case 'redirect.jpg':
      return {
        'status_code': 303,
        'location': 'http://example.org/',
      };
    case 'reduce.jpg':
      return {
        'status_code': 302,
        'scale_numerator': 1,
        'scale_denominator': 2,
      };
    default:
      return true;
  }
};

CustomDelegate.prototype.getAzureStorageSourceBlobKey = function()  {
  switch (this.ctx.identifier) {
    case "missing":
      return;
    case "jpeg.jpg":
      return "jpg";
    default:
      return this.ctx.identifier;
  }
};

CustomDelegate.prototype.getExtraIIIFInformationResponseKeys = function()  {
  switch (this.ctx.identifier) {
    case "bogus":
      return;
    case "empty":
      return {};
    default:
      return {
        'attribution': 'Copyright My Great Organization. All rights reserved.',
        'license': 'http://example.org/license.html',
        'service': {
          '@context': 'http://iiif.io/api/annex/services/physdim/1/context.json',
          'profile': 'http://iiif.io/api/annex/services/physdim',
          'physicalScale': 0.0025,
          'physicalUnits': 'in'
        }
      };
  }
};

CustomDelegate.prototype.getFilesystemSourcePathname = function()  {
  switch (this.ctx.identifier) {
    case 'missing':
      return;
    case 'FilesystemSourceTest-extension-in-identifier-but-not-filename.jpg':
      return 'jpg';
    default:
      return this.ctx.identifier;
  }
};

CustomDelegate.prototype.getHttpSourceResourceInfo = function()  {
  var ident = this.ctx.identifier;

  // ############################ DelegateProxyTest ############################
  if (ident === 'DelegateProxyTest-String') {
    return 'http://example.org/foxes';
  } else if (ident === 'DelegateProxyTest-Hash') {
    return { uri: 'http://example.org/birds' };
  }

  // ########################### HttpSourceTest ################################
  if (ident === 'HttpSourceTest-extension-in-identifier-but-not-filename.jpg') {
    return 'jpg';
  }
  if (ident.startsWith('http://localhost') ||
      ident.startsWith('https://localhost')) {
    // Supply a localhost URL to return the same URL.
    return {
      'uri': identifier,
      'headers': {
        'X-Custom': 'yes'
      }
    };
  } else if (ident.startsWith("valid-auth-")) {
    // Supply a valid URL prefixed with "valid-auth-" to return a valid URL
    // with valid auth info.
    return {
      'uri': ident.replace('valid-auth-', ''),
      'username': 'user',
      'secret': 'secret',
    };
  } else if (ident.startsWith("invalid-auth-")) {
    // Supply a valid URL prefixed with "invalid-auth-" to return a valid URL
    // with invalid auth info.
    return {
      'uri': ident.replace('invalid-auth-', ''),
      'username': 'user',
      'secret': 'bogus'
    };
  } else if (this.ctx.client_ip === '1.2.3.4') {
    if (this.ctx.request_headers['X-Forwarded-Proto'] === 'https') {
      return {
        'uri': 'https://other-example.org/bleh/' + encodeURIComponent(ident)
      };
    } else {
      return {
        'uri': 'http://other-example.org/bleh/' + encodeURIComponent(ident)
      };
    }
  }

  switch (ident) {
    case 'http-jpg-rgb-64x56x8-baseline.jpg':
      return {
        'uri': 'http://example.org/bla/' + encodeURIComponent(identifier),
        'headers': {
          'X-Custom': 'yes'
        }
      };
    case 'http-jpg-rgb-64x56x8-plane.jpg':
      return {
        'uri': 'http://example.org/bla/' + encodeURIComponent(identifier),
        'username': 'username',
        'secret': 'secret',
        'headers': {
          'X-Custom': 'yes'
        }
      };
    case 'https-jpg-rgb-64x56x8-plane.jpg':
      return {
        'uri': 'https://example.org/bla/' + URI.escape(identifier),
        'username': 'username',
        'secret': 'secret',
        'headers': {
          'X-Custom': 'yes'
        }
      };
  }
};

CustomDelegate.prototype.getJdbcSourceDatabaseIdentifier = function()  {
  return this.ctx.identifier;
};

CustomDelegate.prototype.getJdbcSourceMediaType = function()  {
  return "SELECT media_type FROM items WHERE filename = ?";
};

CustomDelegate.prototype.getJdbcSourceLookupSQL = function()  {
  return 'SELECT image FROM items WHERE filename = ?';
};

CustomDelegate.prototype.getMetadata = function()  {
  if (this.ctx.identifier === 'metadata') {
    return '<rdf:RDF>derivative metadata</rdf:RDF>';
  }
};

CustomDelegate.prototype.getOverlayProperties = function()  {
  switch (this.ctx.identifier) {
    case 'image':
      return {
        'image': '/dev/cats',
        'inset': 5,
        'position': 'bottom left'
      };
    case 'string':
      return {
        'background_color': 'rgba(12, 23, 34, 45)',
        'string': "dogs\ndogs",
        'inset': 5,
        'position': 'bottom left',
        'color': 'red',
        'font': 'SansSerif',
        'font_size': 20,
        'font_min_size': 11,
        'font_weight': 1.5,
        'glyph_spacing': 0.1,
        'stroke_color': 'blue',
        'stroke_width': 3
      };
  }
};

CustomDelegate.prototype.getRedactions = function()  {
  switch (this.ctx.identifier) {
    case "bogus":
      return;
    case "empty":
      return [];
    default:
      return [ { x: 0, y: 10, width: 50, height: 50} ];
  }
};

CustomDelegate.prototype.getSource = function()  {
  switch (this.ctx.identifier) {
    case 'http':
      return 'HttpSource';
    case 'jdbc':
      return 'JdbcSoure';
    case 'bogus':
      return;
    default:
      return 'FilesystemSource';
  }
};

CustomDelegate.prototype.getS3SourceObjectInfo = function()  {
  var ident = this.ctx.identifier;
  if (ident.indexOf("bucket:") > -1 || ident.indexOf("key:") > -1) {
    var parts = ident.split(';');
    var struct = {};
    parts.forEach(function(p) {
      var kv = p.split(":");
      struct[kv[0]] = kv[1];
    });
    return struct;
  } else if (ident === 'bogus') {
    return;
  } else {
    return {
      'key': ident,
      'bucket': 'test.cantaloupe.library.illinois.edu'
    }
  }
};
