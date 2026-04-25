import os
import glob

relatorios_link = '                    <a href="/relatorios" class="sidebar-nav-link"><svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg><span class="whitespace-nowrap">Relatórios</span></a>\n'

files = glob.glob(r'c:\Users\kkguiii\Desktop\Microservice\Amb\src\main\resources\templates\*.html')

for f in files:
    if f.endswith('__layout.html') or f.endswith('relatorios.html') or f.endswith('login.html') or f.endswith('index.html'):
        continue
    
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
        
    if '/relatorios' in content:
        continue
        
    new_content = content.replace('Estoque</span>\n                    </a>', 'Estoque</span>\n                    </a>\n' + relatorios_link)
    new_content = new_content.replace('Estoque</span></a>', 'Estoque</span></a>\n' + relatorios_link)
    
    if content != new_content:
        with open(f, 'w', encoding='utf-8') as file:
            file.write(new_content)
        print("Updated", f)
