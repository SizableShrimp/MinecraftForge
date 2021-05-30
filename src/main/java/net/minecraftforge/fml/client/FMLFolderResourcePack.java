/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.fml.client;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import net.minecraft.client.resources.ResourcePackFileNotFoundException;
import net.minecraftforge.fml.common.FMLLog;

import javax.imageio.ImageIO;

import net.minecraft.client.resources.FolderResourcePack;
import net.minecraftforge.fml.common.FMLContainerHolder;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.filefilter.DirectoryFileFilter;

public class FMLFolderResourcePack extends FolderResourcePack implements FMLContainerHolder {

    private ModContainer container;
    private List<File> sources;

    public FMLFolderResourcePack(ModContainer container)
    {
        super(container.getSource());
        this.container = container;
        this.sources = new ArrayList<>(container.getAdditionalSources());
        sources.add(container.getSource());
    }

    @Override
    protected boolean hasResourceName(String p_110593_1_)
    {
        return getResourceFile(p_110593_1_) != null;
    }
    @Override
    public String getPackName()
    {
        return "FMLFileResourcePack:"+container.getName();
    }
    @Override
    protected InputStream getInputStreamByName(String resourceName) throws IOException
    {
        try
        {
            File sub = this.getResourceFile(resourceName);

            if (sub == null)
            {
                throw new ResourcePackFileNotFoundException(this.resourcePackFile, resourceName);
            }
            else
            {
                return new BufferedInputStream(new FileInputStream(sub));
            }
        }
        catch (IOException ioe)
        {
            if ("pack.mcmeta".equals(resourceName))
            {
                FMLLog.log.debug("Mod {} is missing a pack.mcmeta file, substituting a dummy one", container.getName());
                return new ByteArrayInputStream(("{\n" +
                        " \"pack\": {\n"+
                        "   \"description\": \"dummy FML pack for "+container.getName()+"\",\n"+
                        "   \"pack_format\": 2\n"+
                        "}\n" +
                        "}").getBytes(StandardCharsets.UTF_8));
            }
            else throw ioe;
        }
    }

    @Override
    public BufferedImage getPackImage() throws IOException
    {
        return ImageIO.read(getInputStreamByName(container.getMetadata().logoFile));
    }

    @Override
    public ModContainer getFMLContainer()
    {
        return container;
    }

    private File getResourceFile(String resourceName) {
        try
        {
            for (File source : sources)
            {
                File sub = new File(source, resourceName);

                if (sub.isFile() && validatePath(sub, resourceName)) {
                    return sub;
                }
            }
        }
        catch (IOException e)
        {
        }

        return null;
    }

    @Override
    public Set<String> getResourceDomains()
    {
        Set<String> set = Sets.<String>newHashSet();

        for (File source : sources)
        {
            File assets = new File(source, "assets/");

            if (assets.isDirectory()) {
                for (File file2 : assets.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY)) {
                    String s = getRelativeName(assets, file2);

                    if (s.equals(s.toLowerCase(java.util.Locale.ROOT))) {
                        set.add(s.substring(0, s.length() - 1));
                    } else {
                        this.logNameNotLowercase(s);
                    }
                }
            }
        }

        return set;
    }
}
