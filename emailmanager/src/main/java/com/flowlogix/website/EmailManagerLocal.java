/*
 * Copyright (C) 2011-2024 Flow Logix, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flowlogix.website;

import jakarta.ejb.Local;
import jakarta.mail.MessagingException;

@Local
public interface EmailManagerLocal {
    int eraseFolder(String folderName) throws MessagingException;
    boolean isMock();

    int sendDrafts(String draftFolderName, String sentFolderName) throws MessagingException;

    void pingImap() throws MessagingException;
    void pingSmtp() throws MessagingException;
}
