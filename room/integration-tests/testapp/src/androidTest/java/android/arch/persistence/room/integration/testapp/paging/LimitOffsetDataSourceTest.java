/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.arch.persistence.room.integration.testapp.paging;

import static android.test.MoreAsserts.assertEmpty;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import android.arch.persistence.room.integration.testapp.test.TestDatabaseTest;
import android.arch.persistence.room.integration.testapp.test.TestUtil;
import android.arch.persistence.room.integration.testapp.vo.User;
import android.arch.util.paging.ContiguousDataSource;
import android.arch.util.paging.DataSource;
import android.support.annotation.NonNull;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class LimitOffsetDataSourceTest extends TestDatabaseTest {

    @After
    public void teardown() {
        mUserDao.deleteEverything();
    }

    private ContiguousDataSource<User> loadUsersByAgeDesc() {
        return (ContiguousDataSource<User>) mUserDao.loadUsersByAgeDesc();
    }

    @Test
    public void emptyPage() {
        DataSource<User> dataSource = loadUsersByAgeDesc();
        assertThat(dataSource.loadCount(), is(0));
    }

    @Test
    public void loadCount() {
        createUsers(6);
        DataSource<User> dataSource = loadUsersByAgeDesc();
        assertThat(dataSource.loadCount(), is(6));
    }

    @Test
    public void singleItem() {
        List<User> users = createUsers(1);
        ContiguousDataSource<User> dataSource = loadUsersByAgeDesc();
        assertThat(dataSource.loadCount(), is(1));
        List<User> initial = dataSource.loadAfterInitial(0, 10);
        assertThat(initial.get(0), is(users.get(0)));

        assertEmpty(dataSource.loadBefore(0, initial.get(0), 10));
        assertEmpty(dataSource.loadAfter(0, initial.get(0), 10));
    }

    @Test
    public void initial() {
        List<User> users = createUsers(10);
        ContiguousDataSource<User> dataSource = loadUsersByAgeDesc();
        assertThat(dataSource.loadCount(), is(10));
        List<User> initial = dataSource.loadAfterInitial(0, 1);
        assertThat(initial.get(0), is(users.get(0)));
        List<User> second = dataSource.loadAfterInitial(1, 1);
        assertThat(second.get(0), is(users.get(1)));
    }

    @Test
    public void loadAll() {
        List<User> users = createUsers(10);

        ContiguousDataSource<User> dataSource = loadUsersByAgeDesc();
        List<User> all = dataSource.loadAfterInitial(0, 10);
        assertThat(users, is(all));
    }

    @Test
    public void loadAfter() {
        List<User> users = createUsers(10);
        ContiguousDataSource<User> dataSource = loadUsersByAgeDesc();
        List<User> result = dataSource.loadAfter(3, users.get(3), 2);
        assertThat(result, is(users.subList(4, 6)));
    }

    @Test
    public void loadBefore() {
        List<User> users = createUsers(10);
        ContiguousDataSource<User> dataSource = loadUsersByAgeDesc();
        List<User> result = dataSource.loadBefore(5, users.get(5), 3);
        List<User> expected = new ArrayList<>(users.subList(2, 5));
        Collections.reverse(expected);
        assertThat(result, is(expected));
    }

    @Test
    public void loadBefore_limitTest() {
        List<User> users = createUsers(10);
        ContiguousDataSource<User> dataSource = loadUsersByAgeDesc();
        List<User> result = dataSource.loadBefore(5, users.get(5), 10);
        List<User> expected = new ArrayList<>(users.subList(0, 5));
        Collections.reverse(expected);
        assertThat(result, is(expected));
    }

    @NonNull
    private List<User> createUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            User user = TestUtil.createUser(i);
            user.setAge(1);
            mUserDao.insert(user);
            users.add(user);
        }
        return users;
    }
}
