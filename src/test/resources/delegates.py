from urllib import urlencode

class CustomDelegate:
    def set_context(self, ctx):
        self.ctx = ctx

    def authorize(self):
        ident = self.ctx['identifier']
        if ident in ('forbidden.jpg', 'forbidden-boolean.jpg'):
            return False
        elif ident == 'forbidden-code.jpg':
            return {
                'status_code': 401,
                'challenge': 'Basic',
            }
        elif ident == 'redirect.jpg':
            return {
                'status_code': 303,
                'location': 'http://example.org/',
            }
        elif ident == 'reduce.jpg':
            return {
                'status_code': 302,
                'scale_numerator': 1,
                'scale_denominator': 2,
            }
        return True

    def azurestoragesource_blob_key(self):
        ident = self.ctx['identifier']
        if ident == "missing":
            return
        elif ident == "jpeg.jpg":
            return "jpg";
        else:
            return ident

    def extra_iiif2_information_response_keys(self):
        ident = self.ctx['identifier']
        if ident == 'bogus':
            return
        elif ident == 'empty':
            return {}
        else:
            return {
                'attribution': 'Copyright My Great Organization. All rights reserved.',
                'license': 'http://example.org/license.html',
                'service': {
                    '@context': 'http://iiif.io/api/annex/services/physdim/1/context.json',
                    'profile': 'http://iiif.io/api/annex/services/physdim',
                    'physicalScale': 0.0025,
                    'physicalUnits': 'in'
                }
            }

    def filesystemsource_pathname(self):
        ident = self.ctx['identifier']
        if ident == 'missing':
            return
        elif ident == 'FilesystemSourceTest-extension-in-identifier-but-not-filename.jpg':
            return 'jpg'
        else:
            return ident

    def httpsource_resource_info(self):
        ident = self.ctx['identifier']
        if ident == 'DelegateProxyTest-String':
            return 'http://example.org/foxes'
        elif ident == 'DelegateProxyTest-Hash':
            return { 'uri': 'http://example.org/birds' }

        if ident == 'HttpSourceTest-extension-in-identifier-but-not-filename.jpg':
            return 'jpg'

        if ident.startswith('http://localhost') or ident.startswith('https://localhost'):
            return {
                'uri': ident,
                'headers': { 'X-Custom': 'yes' }
            }
        elif ident.startswith('valid-auth'):
            return {
                'uri': ident.replace('valid-auth-', ''),
                'username': 'user',
                'secret': 'secret',
            }
        elif ident.startswith('invalid-auth'):
            return {
                'uri': ident.replace('invalid-auth-', ''),
                'username': 'user',
                'secret': 'bogus'
            }
        elif self.ctx.get('client_ip') == '1.2.3.4':
            headers = self.ctx.get('request_headers')
            if headers and headers.get('X-Forwarded-Proto') == 'https':
                return {
                    'uri': ident.replace('invalid-auth-', ''),
                    'username': 'user',
                    'secret': 'bogus'
                }
            else:
                return {
                    'uri': 'http://other-example.org/bleh/' + urlencode(ident)
                }

        if ident == 'http-jpg-rgb-64x56x8-baseline.jpg':
            return {
                'uri': 'http://example.org/bla/' + urlencode(ident),
                'headers': {
                    'X-Custom': 'yes'
                }
            }
        elif ident == 'http-jpg-rgb-64x56x8-plane.jpg':
            return {
                'uri': 'http://example.org/bla/' + urlencode(ident),
                'username': 'username',
                'secret': 'secret',
                'headers': {
                    'X-Custom': 'yes'
                }
            }
        elif ident == 'https-jpg-rgb-64x56x8-plane.jpg':
            return {
                'uri': 'https://example.org/bla/' + urlencode(ident),
                'username': 'username',
                'secret': 'secret',
                'headers': {
                    'X-Custom': 'yes'
                }
            }

    def jdbcsource_database_identifier(self):
        return self.ctx['identifier']

    def jdbcsource_media_type(self):
        return "SELECT media_type FROM items WHERE filename = ?"

    def jdbcsource_lookup_sql(self):
        return 'SELECT image FROM items WHERE filename = ?'

    def metadata(self):
        if self.ctx['identifier'] == 'metadata':
            return '<rdf:RDF>derivative metadata</rdf:RDF>';

    def overlay(self):
        ident = self.ctx['identifier']
        if ident == 'image':
            return {
                'image': '/dev/cats',
                'inset': 5,
                'position': 'bottom left'
            }
        elif ident == 'string':
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
            }

    def redactions(self):
        ident = self.ctx['identifier']
        if ident == 'bogus':
            return
        elif ident == 'empty':
            return []
        else:
            return [ { 'x': 0, 'y': 10, 'width': 50, 'height': 50 } ]

    def source(self):
        ident = self.ctx['identifier']
        if ident == 'http':
            return 'HttpSource'
        elif ident == 'jdbc':
            return 'JdbcSource'
        elif ident == 'bogus':
            return
        else:
            return 'FilesystemSource'

    def s3source_object_info(self):
        ident = self.ctx['identifier']
        if 'bucket:' in ident or 'key:' in ident:
            struct = {}
            for part in ident.split(';'):
                k, v = part.split(':')
                struct[k] = v
            return struct
        elif ident == 'bogus':
            return
        else:
            return {
                'key': ident,
                'bucket': 'test.cantaloupe.library.illinois.edu'
            }
