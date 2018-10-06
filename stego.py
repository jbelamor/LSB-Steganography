from PIL import Image
from math import ceil
'''
De cada pixel se va a coger el valor de cada canal(R, G, B), se va a convertir a binario
y el ultimo valor del binario va a ser un bit del payload a cargar.
De esta manera en un pizel podremos esconder 3 bits de informacion. Así que por cada byte serian
necesarios 2'6 pixeles. De manera redonda, por cada 8 pixeles puedo esconder 3 bytes. 

Los 6 primeros pixeles se usaran para definir el tamaño del payload en bits. Con 6 pixeles se
puede indicar hasta un tamaño de 6*3 = 18 bits, con lo que se puede representar hasta 
2¹⁸=262144 bits, que son exactamente 32 Megabytes.

Si quisiesemos esconder 32MB (maximo permitido segun el protocolo elegido), serian necesarios
al menos 262144/3 ~ 87382 píxeles + 6 para el tamaño, en total 87388 píxeles.
'''

max_bits_payload = 262144
pixels_needed_size = 6
bpp = 3 #bpp = bits per pixel

class Stego:
    def __init__(self, pic_path):
        self.im = Image.open(pic_path)
        self.w = self.im.width
        self.h = self.im.height
        self.size = self.w * self.h
        
    def transform_bitstring(self, bits, n_final):
        return ''.join('0' for i in range(n_final-len(str(bits)))) + str(bits)

    def get_payload_bins_from_file(self, payload_path):
        payload = open(payload_path, 'rb')
        pb = [self.transform_bitstring(bin(_)[2:],8) for _ in payload.read()]
        return pb

    def get_payload_bins_from_txt(self, text):
        pb = [self.transform_bitstring(bin(_)[2:],8) for _ in text]
        return pb

    def check_sizes(self, im_size, payload):
        if len(payload) > max_bits_payload:
            print('Payload too big')
            exit(1)
        elif len(payload) > (self.size + pixels_needed_size)*3:
            print('The picture is not big enough for the payload given')
            exit(1)
            
    def hide(self, payload, output_path=None):
        pixels_list = list(self.im.getdata())
        final_size = self.transform_bitstring(bin(len(payload))[2:], pixels_needed_size * bpp)
        size_pixels = pixels_list[:pixels_needed_size]
        payload_pixels = pixels_list[pixels_needed_size:]
        flat_payload = ''.join(_ for _ in payload)
        #hide payload size
        # print(final_size)
        for i in range(pixels_needed_size):
            r, g, b = [bin(_)[2:] for _ in pixels_list[i]]
            r = r[:-1] + final_size[i * 3]
            g = g[:-1] + final_size[(i * 3) + 1]
            b = b[:-1] + final_size[(i * 3) + 2]
            size_pixels[i] = (int(r, 2), int(g, 2), int(b, 2))

        # print(flat_payload)
        #hide payload
        for i in range(ceil(len(flat_payload)/bpp)):
            r, g, b = [bin(_)[2:] for _ in payload_pixels[i]] 
            r = r[:-1] + flat_payload[i * 3]
            try:
                g = g[:-1] + flat_payload[(i * 3) + 1]
                b = b[:-1] + flat_payload[(i * 3) + 2]
            except:
                pass
            payload_pixels[i] = (int(r, 2), int(g, 2), int(b, 2))

        modified_pixels = size_pixels + payload_pixels
        final_pic = Image.new(self.im.mode, self.im.size)
        final_pic.putdata(modified_pixels[:])
        if output_path:
            new_path = self.im.filename.split('/')[-1].split('.')[0]
            print(output_path)
            final_pic.save(output_path + '/' + new_path + '.png', 'PNG')
        else:
            final_pic.save(self.im.filename + '.stegued', 'PNG')
        final_pic.close()
        self.im.close()

    def reveal(self):
        pixels_list = list(self.im.getdata())
        final_binary_size = ''
        flat_payload = ''
        splited_payload = []
        
        #find payload size
        for i in range(pixels_needed_size):
            r, g, b = [bin(_)[2:] for _ in pixels_list[i]]
            final_binary_size += r[-1] + g[-1] + b[-1]

        final_size = int(final_binary_size, 2)
        print(final_size)
        
        #remove the pixels that contain the size of the payload
        pixels_list = pixels_list[pixels_needed_size:]
        
        #extract payload
        for i in range(ceil(final_size*8/bpp)):
            r, g, b = [bin(_)[2:] for _ in pixels_list[i]]
            flat_payload += r[-1] + g[-1] + b[-1]
            
        #unflat and export to file
        cont = 0
        aux = ''
        result = open('hidden_payload.txt', 'w')
        for n in flat_payload:
            aux += n
            cont +=1
            if cont % 8 == 0:
                result.write(chr(int(aux, 2)))            
                # splited_payload.append(aux)
                aux=''
        # print(splited_payload)
        result.close()
        with open('hidden_payload.txt', 'r') as pp:
            print(pp.read())



# st=Stego('new.jpg')
# # st2=Stego('new.png')
# st2=Stego('../resul.png')
# # st.hide(st.get_payload_bins_from_file('test_payload.txt'))
# st2.reveal()
